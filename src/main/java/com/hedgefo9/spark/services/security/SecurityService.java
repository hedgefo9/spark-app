package com.hedgefo9.spark.services.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    public Optional<CustomUserDetails> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Current authentication: {}", authentication);

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            logger.info("Found authenticated user: {}", authentication.getPrincipal());
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }

        logger.info("No authenticated user found");
        return Optional.empty();
    }

    public void setAuthentication(CustomUserDetails userDetails, String token) {
        logger.info("Setting authentication for user: {} with token: {}", userDetails.getUsername(), token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        token,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.info("Authentication set in context: {}", SecurityContextHolder.getContext().getAuthentication());
    }

    public void clearAuthentication() {
        logger.info("Clearing authentication context");
        SecurityContextHolder.clearContext();
    }

    public Optional<String> getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Getting current token from authentication: {}", authentication);

        if (authentication != null && authentication.getDetails() instanceof String) {
            String token = (String) authentication.getDetails();
            logger.info("Found token: {}", token);
            return Optional.of(token);
        }

        logger.info("No token found");
        return Optional.empty();
    }
}