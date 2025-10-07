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
    level=logging.INFO,
    format="%(asctime)s [%(processName)s] %(levelname)s: %(message)s",
    handlers=[logging.StreamHandler()]
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
    url = f"{AUTH_SERVICE}/login"
    resp = requests.post(url, json={"email": email, "password": password})
    if resp.status_code != 200:
        raise Exception(f"Login failed for {email}: {resp.status_code}")
    return resp.json()["token"]


def park(token):
    url = f"{VALET_PARKING_SERVICE}/park"
    resp = requests.post(url, json={"token": token})
    if resp.status_code != 200:
        raise Exception("Park request failed")
    return resp.json()["requestId"]


def retrieve(token):
    url = f"{VALET_PARKING_SERVICE}/retrieve"
    resp = requests.post(url, json={"token": token})
    if resp.status_code != 200:
        raise Exception("Retrieve request failed")
    return resp.json()["requestId"]


def wait_until_complete(request_id):
    url = f"{VALET_PARKING_SERVICE}/{request_id}/status"
    while running:
        time.sleep(5)
        resp = requests.get(url)
        if resp.status_code != 200:
            raise Exception("Status check failed")
        if resp.json().get("finished"):
            break


def wait_random():
    time.sleep(random.randint(10, 20))


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
    with open("users.json") as f:
        return json.load(f)


def main():
    users = load_users()
    max_workers = min(len(users), os.cpu_count() or 4)

    logger.info(f"Starting test for {len(users)} users using {max_workers} processes...")

    with ProcessPoolExecutor(max_workers=max_workers) as executor:
        futures = [executor.submit(user_flow, u["email"], u["password"]) for u in users]
        try:
            for f in as_completed(futures):
                f.result()  # propagate any exceptions
        except KeyboardInterrupt:
            logger.info("Keyboard interrupt detected, shutting down...")
        except Exception as e:
            logger.error(f"Unhandled error: {e}")
        finally:
            executor.shutdown(wait=False)
            logger.info("All user processes stopped.")


if __name__ == "__main__":
    main()
