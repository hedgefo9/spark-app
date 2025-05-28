package com.hedgefo9.spark.controller;

import com.hedgefo9.spark.models.Bio;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.service.BioService;
import com.hedgefo9.spark.service.UserPhotoService;
import com.hedgefo9.spark.service.UserService;
import com.hedgefo9.spark.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserPhotoService userPhotoService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final BioService bioService;

    @GetMapping("/{id}")
    public String show(@PathVariable("id") long id, Model model) {
        User user = userService.getUserById(id);
        var authenticatedUser = securityService.getAuthenticatedUser();
        model.addAttribute("user", user);
        model.addAttribute("bio", bioService.getBioByUserId(id));
        model.addAttribute("photos", userPhotoService.getPhotosByUserId(id));
        if (authenticatedUser.isPresent()) {
            Person person = authenticatedUser.get().person();
            User realUser = (User) person;

            model.addAttribute("isEditable", Objects.equals(user.userId(), realUser.userId()));
        }
        return "show";
    }


    @PostMapping
    public ResponseEntity<?> add(@RequestBody User user) {
        try {
            if (userService.existsByEmail(user.email())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Этот email уже используется"));
            }

            if (userService.existsByPhoneNumber(user.phoneNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Этот телефон уже используется"));
            }

            LocalDate birth_date = user.birthDate().toInstant()
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate();
            int age = Period.between(birth_date, LocalDate.now()).getYears();
            if (age < 18) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Возраст должен быть не менее 18 лет"));
            }

            user.passwordHash(passwordEncoder.encode(user.passwordHash()));

            userService.createUser(user);
            Long userId = userService.getUserByEmail(user.email()).userId();
            return ResponseEntity.ok().body(Map.of("user_id", userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать пользователя: " + e.getMessage()));
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        try {
            var authenticatedUser = securityService.getAuthenticatedUser();
            boolean isAuthenticated = authenticatedUser.isPresent();
            if (!isAuthenticated) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете обновлять только свой профиль");

            }
            Person person = authenticatedUser.get().person();
            User realUser = (User) person;
            if (!Objects.equals(user.userId(), realUser.userId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете обновлять только свой профиль");

            user.updatedAt(LocalDateTime.now());
            if (!userService.existsByEmail(user.email())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }
            boolean updated = userService.updateUser(user);
            if (updated) {
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.badRequest().body("Произошла ошибка при обновлении данных пользователя");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать пользователя: " + e.getMessage()));
        }
    }

    @PostMapping("/bio")
    public ResponseEntity<?> addBio(@RequestBody Bio bio) {
        try {
            if (userService.getUserById(bio.userId()) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }

            System.out.println(bio);
            bioService.addBio(bio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать био пользователя: " + e.getMessage()));
        }
    }

    @GetMapping("/bio")
    public ResponseEntity<?> getBio(@RequestParam Long userId) {
        try {
            if (userService.getUserById(userId) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }

            var authenticatedUser = securityService.getAuthenticatedUser();
            var bio = bioService.getBioByUserId(userId);
            boolean isAuthenticated = authenticatedUser.isPresent();
            if (!isAuthenticated) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Только авторизованные пользователи могут смотреть профили");
            }

            return ResponseEntity.ok().body(bio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать пользователя: " + e.getMessage()));
        }
    }

    @PatchMapping("/bio")
    public ResponseEntity<?> updateBio(@RequestBody Bio bio) {
        try {
            var authenticatedUser = securityService.getAuthenticatedUser();
            boolean isAuthenticated = authenticatedUser.isPresent();

            if (!isAuthenticated) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете обновлять только свой профиль");

            }
            Person person = (Person) authenticatedUser.get().person();
            User realUser = (User) person;


            if (!Objects.equals(bio.userId(), realUser.userId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете обновлять только свой профиль");
            }
            if (userService.getUserById(bio.userId()) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }

            bio.updatedAt(LocalDateTime.now());
            boolean updated = bioService.updateBio(bio);
            if (updated) {
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.badRequest().body("Произошла ошибка при обновлении данных пользователя");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать пользователя: " + e.getMessage()));
        }
    }
}
