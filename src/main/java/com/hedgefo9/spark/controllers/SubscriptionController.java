package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.services.SubscriptionService;
import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.Subscription;
import com.hedgefo9.spark.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SecurityService securityService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SecurityService securityService, SubscriptionService subscriptionService) {
        this.securityService = securityService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<String> addSubscription(@RequestBody Subscription subscription) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Только аутентифицированный пользователь может совершать это действие");
        }

        Person person = authenticatedUser.get().person();
        try {
            Admin admin = (Admin) person;
            subscriptionService.addSubscription(subscription);
            return ResponseEntity.ok("Подписка добавлена успешно");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Недостаточно прав");
        }
    }


    @GetMapping
    public List<Subscription> getAllSubscriptions() {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return List.of();
        }

        Person person = authenticatedUser.get().person();
        try {
            Admin admin = (Admin) person;
            return subscriptionService.findAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    @GetMapping("/{userId}")
    public List<Subscription> getSubscriptions(@PathVariable Long userId) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return List.of();
        }

        Person person = authenticatedUser.get().person();
        return subscriptionService.findByUserId(userId);
    }


    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Long subscriptionId) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Только аутентифицированный пользователь может совершать это действие");
        }

        Person person = authenticatedUser.get().person();
        try {
            Admin admin = (Admin) person;
            subscriptionService.deleteSubscription(subscriptionId);
            return ResponseEntity.ok("Подписка удалена успешно");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Недостаточно прав");
        }
    }
}

