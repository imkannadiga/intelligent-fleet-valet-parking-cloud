package com.example.heartbeat.Repositories;

import com.example.heartbeat.Models.UGV;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UGVRepository extends MongoRepository<UGV, String> {

}
