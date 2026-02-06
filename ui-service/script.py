import os
import time
import json
import random
import signal
import requests
import logging
from dotenv import load_dotenv
from concurrent.futures import ProcessPoolExecutor, as_completed

# Load environment variables
load_dotenv()

AUTH_SERVICE = os.getenv('AUTH_SERVICE')
VALET_PARKING_SERVICE = os.getenv('VALET_PARKING_SERVICE')

# ---------------- Logging Setup ----------------
logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s [%(processName)s] [%(name)s] %(levelname)s: %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('ui-service.log', mode='a')
    ],
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

# ---------------- Graceful Shutdown ----------------
running = True
def handle_shutdown(signum, frame):
    global running
    logger.info("Received shutdown signal, stopping all user flows...")
    running = False

signal.signal(signal.SIGINT, handle_shutdown)
signal.signal(signal.SIGTERM, handle_shutdown)


# ---------------- API Helpers ----------------
def login(email, password):
    logger.debug(f"Attempting login for email: {email}")
    url = f"{AUTH_SERVICE}/login"
    try:
        resp = requests.post(url, json={"email": email, "password": password}, timeout=10)
        if resp.status_code != 200:
            logger.error(f"Login failed for {email}: HTTP {resp.status_code}")
            raise Exception(f"Login failed for {email}: {resp.status_code}")
        token = resp.json()["token"]
        logger.info(f"Login successful for {email}")
        return token
    except requests.exceptions.RequestException as e:
        logger.error(f"Login request failed for {email}: {e}")
        raise


def park(token):
    logger.debug("Sending park request")
    url = f"{VALET_PARKING_SERVICE}/park"
    try:
        resp = requests.post(url, json={"token": token}, timeout=10)
        if resp.status_code != 200:
            logger.error(f"Park request failed: HTTP {resp.status_code}")
            raise Exception("Park request failed")
        request_id = resp.json()["requestId"]
        logger.info(f"Park request successful - Request ID: {request_id}")
        return request_id
    except requests.exceptions.RequestException as e:
        logger.error(f"Park request failed: {e}")
        raise


def retrieve(token):
    logger.debug("Sending retrieve request")
    url = f"{VALET_PARKING_SERVICE}/retrieve"
    try:
        resp = requests.post(url, json={"token": token}, timeout=10)
        if resp.status_code != 200:
            logger.error(f"Retrieve request failed: HTTP {resp.status_code}")
            raise Exception("Retrieve request failed")
        request_id = resp.json()["requestId"]
        logger.info(f"Retrieve request successful - Request ID: {request_id}")
        return request_id
    except requests.exceptions.RequestException as e:
        logger.error(f"Retrieve request failed: {e}")
        raise


def wait_until_complete(request_id):
    logger.debug(f"Waiting for request to complete - Request ID: {request_id}")
    url = f"{VALET_PARKING_SERVICE}/{request_id}/status"
    check_count = 0
    while running:
        time.sleep(5)
        check_count += 1
        try:
            resp = requests.get(url, timeout=10)
            if resp.status_code != 200:
                logger.error(f"Status check failed for request {request_id}: HTTP {resp.status_code}")
                raise Exception("Status check failed")
            status_data = resp.json()
            logger.debug(f"Status check #{check_count} for request {request_id}: finished={status_data.get('finished')}")
            if status_data.get("finished"):
                logger.info(f"Request completed - Request ID: {request_id} after {check_count} checks")
                break
        except requests.exceptions.RequestException as e:
            logger.error(f"Status check request failed for {request_id}: {e}")
            raise


def wait_random():
    wait_time = random.randint(10, 20)
    logger.debug(f"Waiting {wait_time} seconds")
    time.sleep(wait_time)


# ---------------- Main Flow ----------------
def user_flow(email, password):
    logger.info(f"Starting valet flow for {email}")
    while running:
        try:
            token = login(email, password)
            req_id = park(token)
            wait_until_complete(req_id)
            wait_random()
            req_id = retrieve(token)
            wait_until_complete(req_id)
            wait_random()
        except Exception as e:
            logger.error(f"Error for {email}: {e}")
            time.sleep(10)
    logger.info(f"Stopping valet flow for {email}")


def load_users():
    logger.debug("Loading users from users.json")
    try:
        with open("users.json") as f:
            users = json.load(f)
            logger.info(f"Loaded {len(users)} user(s) from users.json")
            return users
    except FileNotFoundError:
        logger.error("users.json file not found")
        raise
    except json.JSONDecodeError as e:
        logger.error(f"Error parsing users.json: {e}")
        raise


def main():
    logger.info("=" * 60)
    logger.info("UI Service starting")
    logger.info("=" * 60)
    
    try:
        users = load_users()
        max_workers = min(len(users), os.cpu_count() or 4)
        logger.info(f"Starting test for {len(users)} user(s) using {max_workers} process(es)")
        logger.debug(f"System CPU count: {os.cpu_count()}")

        with ProcessPoolExecutor(max_workers=max_workers) as executor:
            futures = [executor.submit(user_flow, u["email"], u["password"]) for u in users]
            logger.info(f"Submitted {len(futures)} user flow task(s)")
            try:
                for f in as_completed(futures):
                    try:
                        f.result()  # propagate any exceptions
                    except Exception as e:
                        logger.error(f"User flow task failed: {e}")
            except KeyboardInterrupt:
                logger.warning("Keyboard interrupt detected, shutting down...")
            except Exception as e:
                logger.error(f"Unhandled error in main: {e}", exc_info=True)
            finally:
                logger.info("Shutting down executor...")
                executor.shutdown(wait=False)
                logger.info("All user processes stopped.")
    except Exception as e:
        logger.error(f"Fatal error in main: {e}", exc_info=True)
        raise


if __name__ == "__main__":
    main()
