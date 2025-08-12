package com.example.valetparking.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.valetparking.Models.Occupancy;
import java.util.List;


public interface OccupancyRepository extends MongoRepository<Occupancy, String>{
    List<Occupancy> findByUgvId(String ugvId);
} 
