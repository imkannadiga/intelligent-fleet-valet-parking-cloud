package com.example.auth.Entities.users;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ugvs")
public class UGV {

    @Id
    private String id;

    private String name;
    private String type;
    private UGVStatus status = UGVStatus.UNKNOWN;

    public UGV() {
        this.id = new ObjectId().toString(); // Generates an ObjectId as a String
    }

    public UGV(String name, String type) {
        this.id = new ObjectId().toString(); // Generates an ObjectId as a String
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(UGVStatus status) {
        this.status = status;
    }

    public UGVStatus getStatus() {
        return this.status;
    }

}