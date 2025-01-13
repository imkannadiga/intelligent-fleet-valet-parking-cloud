package com.valetparking.valetparking.helpers;

import com.valetparking.valetparking.entities.UGV;
import com.valetparking.valetparking.misc.Status;
import com.valetparking.valetparking.services.UGVService;

public class UGVHelper {
    
    UGVService ugvService = new UGVService();

    public boolean isUGVIDValid(String UGVId) {

        if(ugvService.getUGVById(UGVId)!=null) 
            return true;

        return false;
    }

    public void saveUGVSession(String UGVId, String sessionId) {

        UGV ugv = ugvService.getUGVById(UGVId);

        ugv.setSessionId(sessionId);

        ugvService.updateUGV(UGVId, ugv);

    }

    public boolean checkUGVStatus(String UGVId, Status status) {

        if(ugvService.getUGVById(UGVId).getStatus() != status) return false;
        return true;

    }
}
 