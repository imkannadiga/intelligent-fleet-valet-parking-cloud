package com.example.auth.Infra.Security;

import com.example.auth.Repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("Processing security filter for request: {} {}", request.getMethod(), request.getRequestURI());
        
        var token = this.recoverToken(request);
        if (token != null) {
            logger.debug("Token found in request header");
            var subject = tokenService.validateToken(token);
            
            if (subject != null && !subject.isEmpty()) {
                logger.debug("Token validated, loading user: {}", subject);
                UserDetails user = userRepository.findByEmail(subject);

                if (user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set in security context for user: {}", subject);
                } else {
                    logger.warn("User not found for validated token subject: {}", subject);
                }
            } else {
                logger.debug("Token validation failed or returned empty subject");
            }
        } else {
            logger.debug("No token found in request header");
        }
        
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            logger.trace("Authorization header not found");
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        logger.trace("Token recovered from Authorization header");
        return token;
    }
}