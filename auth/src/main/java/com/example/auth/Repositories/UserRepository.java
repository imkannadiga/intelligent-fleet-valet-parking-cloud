package com.example.auth.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.auth.Entities.users.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    public User findByEmail(String email);

}
