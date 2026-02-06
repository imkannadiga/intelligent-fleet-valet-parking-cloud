package com.example.auth.Infra.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.auth.Entities.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        logger.debug("Generating token for user: {}", user.getEmail());
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.secret);
            Instant expirationDate = generateExpirationDate();
            logger.debug("Token expiration date: {}", expirationDate);
            
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(expirationDate)
                    .sign(algorithm);

            logger.info("Token generated successfully for user: {}", user.getEmail());
            return token;

        } catch (JWTCreationException exception) {
            logger.error("Error while generating token for user: {}", user.getEmail(), exception);
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateToken(String token) {
        logger.debug("Validating token");
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

            logger.debug("Token validated successfully for subject: {}", subject);
            return subject;

        } catch (JWTVerificationException exception) {
            logger.warn("Token validation failed: {}", exception.getMessage());
            return "";
        }
    }

    private Instant generateExpirationDate() {
        Instant expiration = LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
        logger.debug("Generated token expiration date: {}", expiration);
        return expiration;
    }
}