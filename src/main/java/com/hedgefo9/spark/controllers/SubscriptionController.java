package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.SubscriptionsDAO;
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
    private final SubscriptionsDAO subscriptionsDAO;

    @Autowired
    public SubscriptionController(SecurityService securityService, SubscriptionsDAO subscriptionsDAO) {
        this.securityService = securityService;
        this.subscriptionsDAO = subscriptionsDAO;
    }

    /*
    @GetMapping("/{userId}")
    public ResponseEntity<?> getSubscriptionsByUserId(@PathVariable Long userId) {
        List<Subscription> subscriptions = subscriptionsDAO.findByUserId(userId);
        for (var s : subscriptions) {
            if (Instant.now().isAfter(s.startAt().toInstant())
                    && Instant.now().isBefore(s.endAt().toInstant())) {
                return ResponseEntity.ok(s);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addSubscription(@RequestBody Subscription subscription) {
        subscriptionsDAO.addSubscription(subscription);
        return ResponseEntity.ok("Подписка успешно добавлена!");
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Long subscriptionId) {
        subscriptionsDAO.deleteSubscription(subscriptionId);
        return ResponseEntity.ok("Подписка успешно удалена!");
    }

    @GetMapping("/details/{subscriptionId}")
    public ResponseEntity<Subscription> getSubscriptionDetails(@PathVariable Long subscriptionId) {
        Subscription subscription = subscriptionsDAO.findById(subscriptionId);
        return ResponseEntity.ok(subscription);
    } */

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
            subscriptionsDAO.addSubscription(subscription);
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
            return subscriptionsDAO.findAll();
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
        return subscriptionsDAO.findByUserId(userId);
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
            subscriptionsDAO.deleteSubscription(subscriptionId);
            return ResponseEntity.ok("Подписка удалена успешно");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Недостаточно прав");
        }
    }
}

