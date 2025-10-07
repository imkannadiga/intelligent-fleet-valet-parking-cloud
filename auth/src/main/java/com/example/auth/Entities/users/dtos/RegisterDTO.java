package com.example.auth.Entities.users.dtos;

import com.example.auth.Entities.users.UserRole;

import java.util.Map;

public record RegisterDTO(String name, String email, String password, UserRole role, Map<String, Object> ugvDetails) {

}