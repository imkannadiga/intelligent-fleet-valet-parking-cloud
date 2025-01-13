package com.valetparking.valetparking.repositories;

import com.valetparking.valetparking.entities.UGV;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UGVRepository extends MongoRepository<UGV, String> {

}
