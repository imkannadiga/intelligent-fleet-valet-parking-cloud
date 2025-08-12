package com.example.valetparking.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.valetparking.Helpers.Phase;
import com.example.valetparking.Helpers.Task;

@Document(collection = "valet_parking_requests")
public class Requests {
    
    @Id
    private String id;

    private String ugvId;

    private Phase currentPhase;

    private Task currentTask;

    private String currentJobId;

    private String parkingSpotId;

    private boolean schedulerFlag;

    private boolean finished;

    public String getUgvId() {
        return ugvId;
    }

    public void setUgvId(String ugvId) {
        this.ugvId = ugvId;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public String getCurrentJobId() {
        return currentJobId;
    }

    public void setCurrentJobId(String currentJobId) {
        this.currentJobId = currentJobId;
    }

    public String getParkingSpotId() {
        return parkingSpotId;
    }

    public void setParkingSpotId(String parkingSpotId) {
        this.parkingSpotId = parkingSpotId;
    }

    public boolean isSchedulerFlag() {
        return schedulerFlag;
    }

    public void setSchedulerFlag(boolean schedulerFlag) {
        this.schedulerFlag = schedulerFlag;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    
    

}
