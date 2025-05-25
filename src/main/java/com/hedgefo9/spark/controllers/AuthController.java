package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.services.security.CustomUserDetails;
import com.hedgefo9.spark.services.security.SecurityService;
import com.hedgefo9.spark.services.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final SecurityService securityService;

    public AuthController(AuthenticationManager authenticationManager,
                          TokenService tokenService,
                          SecurityService securityService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.securityService = securityService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = tokenService.createToken(username);

        securityService.setAuthentication(userDetails, token);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        String authToken = bearerToken.substring(7);
        tokenService.removeToken(authToken);
        securityService.clearAuthentication();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authToken) {
        if (authToken != null && !tokenService.validateToken(authToken)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().build();
    }
}