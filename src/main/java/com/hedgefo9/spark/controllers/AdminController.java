package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.AdminsDAO;
import com.hedgefo9.spark.dao.SubscriptionsDAO;
import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.Subscription;
import com.hedgefo9.spark.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final SecurityService securityService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminsDAO adminsDAO;

    @Autowired
    public AdminController(SecurityService securityService, BCryptPasswordEncoder passwordEncoder, AdminsDAO adminsDAO) {
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
        this.adminsDAO = adminsDAO;
    }

    @GetMapping("/")
    public List<Admin> getAllAdmins() {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return List.of();
        }

        Person person = authenticatedUser.get().person();
        try {
            Admin admin = (Admin) person;
            return adminsDAO.findAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @PostMapping("/")
    public ResponseEntity<String> addAdmin(@RequestBody Admin admin) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("У вас недостаточно прав или вы вошли не как админ");
        }

        Person person = authenticatedUser.get().person();
        try {
            Admin authedAdmin = (Admin) person;
            admin.passwordHash(passwordEncoder.encode(admin.passwordHash()));
            adminsDAO.save(admin);
            return ResponseEntity.ok("Админ добавлен успешно");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас недостаточно прав или вы вошли не как админ");
        }

    }
}

