package com.example.controlserver.Services;

import com.example.controlserver.Models.UGV;
import com.example.controlserver.Misc.UGVStatus;
import com.example.controlserver.Repositories.UGVRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UGVService {

    private static final Logger logger = LoggerFactory.getLogger(UGVService.class);

    @Autowired
    private UGVRepository ugvRepository;

    public UGV saveUGV(UGV ugv) {
        logger.debug("Saving UGV - Name: {}, Type: {}", ugv.getName(), ugv.getType());
        UGV saved = ugvRepository.save(ugv);
        logger.info("UGV saved successfully - ID: {}, Name: {}", saved.getId(), saved.getName());
        return saved;
    }

    public List<UGV> getAllUGVs() {
        logger.debug("Fetching all UGVs");
        List<UGV> ugvs = ugvRepository.findAll();
        logger.info("Retrieved {} UGV(s)", ugvs.size());
        return ugvs;
    }

    public UGV getUGVById(String id) {
        logger.debug("Fetching UGV by ID: {}", id);
        return ugvRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("UGV not found - ID: {}", id);
                    return new RuntimeException("UGV not found");
                });
    }

    public UGV updateUGV(String id, UGV ugvDetails) {
        logger.debug("Updating UGV - ID: {}", id);
        UGV ugv = getUGVById(id);
        ugv.setName(ugvDetails.getName());
        ugv.setType(ugvDetails.getType());
        ugv.setStatus(ugvDetails.getStatus());
        UGV updated = ugvRepository.save(ugv);
        logger.info("UGV updated successfully - ID: {}, Name: {}", id, updated.getName());
        return updated;
    }

    public void deleteUGV(String id) {
        logger.info("Deleting UGV - ID: {}", id);
        ugvRepository.deleteById(id);
        logger.info("UGV deleted successfully - ID: {}", id);
    }

    public UGV setUGVSessionID(String id, String sessionId) {
        logger.debug("Setting session ID for UGV - ID: {}, Session ID: {}", id, sessionId);
        UGV ugv = getUGVById(id);
        ugv.setSessionId(sessionId);
        UGV updated = ugvRepository.save(ugv);
        logger.info("Session ID set successfully - UGV ID: {}, Session ID: {}", id, sessionId);
        return updated;
    }

    public boolean isUGVIDValid(String UGVId) {
        logger.debug("Validating UGV ID: {}", UGVId);
        try {
            if(this.getUGVById(UGVId) != null) {
                logger.debug("UGV ID is valid - {}", UGVId);
                return true;
            }
        } catch (Exception e) {
            logger.debug("UGV ID is invalid - {}", UGVId);
        }
        return false;
    }

    public void saveUGVSession(String UGVId, String sessionId) {
        logger.debug("Saving UGV session - UGV ID: {}, Session ID: {}", UGVId, sessionId);
        UGV ugv = this.getUGVById(UGVId);
        ugv.setSessionId(sessionId);
        this.updateUGV(UGVId, ugv);
        logger.info("UGV session saved successfully - UGV ID: {}, Session ID: {}", UGVId, sessionId);
    }

    public boolean checkUGVStatus(String UGVId, UGVStatus status) {
        logger.debug("Checking UGV status - UGV ID: {}, Expected Status: {}", UGVId, status);
        try {
            UGV ugv = this.getUGVById(UGVId);
            boolean matches = ugv.getStatus() == status;
            logger.debug("UGV status check - UGV ID: {}, Current: {}, Expected: {}, Matches: {}", 
                    UGVId, ugv.getStatus(), status, matches);
            return matches;
        } catch (Exception e) {
            logger.error("Error checking UGV status - UGV ID: {}", UGVId, e);
            return false;
        }
    }

}
