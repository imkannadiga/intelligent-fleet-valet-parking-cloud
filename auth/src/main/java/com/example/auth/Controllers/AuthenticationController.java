package com.example.auth.Controllers;

import com.example.auth.Entities.users.dtos.AuthenticationDTO;
import com.example.auth.Entities.users.dtos.LoginResponseDTO;
import com.example.auth.Entities.users.dtos.RegisterDTO;
import com.example.auth.Entities.users.UGV;
import com.example.auth.Entities.users.User;

import com.example.auth.Infra.Security.TokenService;
import com.example.auth.Repositories.UserRepository;

import com.example.auth.Helpers.ControlHelper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth", produces = { "application/json" })
public class AuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ControlHelper controlHelper;

    /**
     * Authenticates user login.
     *
     * @param data Object containing user credentials
     * @return ResponseEntity containing authentication token
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> login(@RequestBody AuthenticationDTO data) {
        logger.info("Login attempt for email: {}", data.email());
        logger.debug("Processing login request for user: {}", data.email());
        
        try {
            var credentials = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            logger.debug("Attempting authentication for user: {}", data.email());
            
            var auth = this.authenticationManager.authenticate(credentials);
            logger.debug("Authentication successful for user: {}", data.email());

            var token = tokenService.generateToken((User) auth.getPrincipal());
            logger.info("Login successful for email: {}", data.email());
            logger.debug("Token generated successfully for user: {}", data.email());

            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (Exception e) {
            logger.error("Login failed for email: {}", data.email(), e);
            throw e;
        }
    }

    /**
     * Registers a new user.
     *
     * @param data Object containing user registration data
     * @return ResponseEntity indicating success or failure of registration
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> register(@RequestBody RegisterDTO data) {
        logger.info("Registration attempt for email: {}, name: {}", data.email(), data.name());
        logger.debug("Registration data received - email: {}, role: {}", data.email(), data.role());
        
        if (this.userRepository.findByEmail(data.email()) != null) {
            logger.warn("Registration failed: Email already exists - {}", data.email());
            return ResponseEntity.badRequest().build();
        }

        logger.debug("Creating UGV for user: {}", data.email());
        UGV ugv = new UGV(data.ugvDetails().get("name").toString(), data.ugvDetails().get("type").toString());
        logger.debug("UGV created - name: {}, type: {}", ugv.getName(), ugv.getType());

        try {
            logger.debug("Sending UGV creation request to control server");
            controlHelper.createUGV(ugv);
            logger.info("UGV created successfully in control server - name: {}", ugv.getName());
        } catch (Exception e) {
            logger.error("Error creating UGV in control server for user: {}", data.email(), e);
            return ResponseEntity.status(500).body("Error creating UGV");
        }

        logger.debug("Encrypting password for user: {}", data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User user = new User(data.name(), data.email(), encryptedPassword, data.role(), ugv);
        logger.debug("User object created for: {}", data.email());

        logger.debug("Saving user to repository");
        this.userRepository.save(user);
        logger.info("User registered successfully - email: {}, name: {}", data.email(), data.name());

        return ResponseEntity.ok().build();
    }


    /**
     * Validates a token and retrieves associated UGV details.
     *
     * @param payload Map containing the token to be validated
     * @return ResponseEntity containing UGV details or error message
     */
    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> validateToken(@RequestBody Map<String, String> payload) {
        logger.debug("Token validation request received");
        String token = payload.get("token");
        
        if (token == null || token.isEmpty()) {
            logger.warn("Token validation failed: Token is null or empty");
            return ResponseEntity.badRequest().body("Token is required");
        }

        logger.debug("Validating token");
        String email = tokenService.validateToken(token);
        if (email == null || email.isEmpty()) {
            logger.warn("Token validation failed: Invalid or expired token");
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        logger.debug("Token validated successfully for email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("Token validation failed: User not found for email: {}", email);
            return ResponseEntity.status(404).body("User not found");
        }

        UGV ugv = user.getUgv();
        if (ugv == null) {
            logger.warn("Token validation failed: UGV not found for user: {}", email);
            return ResponseEntity.status(404).body("UGV not found for user");
        }

        logger.info("Token validation successful for email: {}, UGV: {}", email, ugv.getId());
        return ResponseEntity.ok(ugv);
    }
}