package com.hedgefo9.spark.controller;

import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.service.security.SecurityService;
import com.hedgefo9.spark.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SecurityService securityService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminService adminService;

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
            return adminService.findAll();
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
            adminService.save(admin);
            return ResponseEntity.ok("Админ добавлен успешно");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас недостаточно прав или вы вошли не как админ");
        }

    }
}
