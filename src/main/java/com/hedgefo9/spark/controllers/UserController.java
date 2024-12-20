package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.BiosDAO;
import com.hedgefo9.spark.dao.UserPhotosDAO;
import com.hedgefo9.spark.dao.UsersDAO;
import com.hedgefo9.spark.models.Bio;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.services.security.SecurityService;
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
public class UserController {

    private final UsersDAO usersDAO;
    private final BiosDAO biosDAO;
    private final UserPhotosDAO userPhotosDAO;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    @Autowired
    public UserController(UsersDAO usersDAO, BiosDAO biosDAO, UserPhotosDAO userPhotosDAO, BCryptPasswordEncoder passwordEncoder, SecurityService securityService) {
        this.usersDAO = usersDAO;
        this.biosDAO = biosDAO;
        this.userPhotosDAO = userPhotosDAO;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id") long id, Model model) {
        User user = usersDAO.show(id);
        var authenticatedUser = securityService.getAuthenticatedUser();
        model.addAttribute("user", user);
        model.addAttribute("bio", biosDAO.getBioByUserId(id));
        model.addAttribute("photos", userPhotosDAO.findByUserId(id));
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
            if (usersDAO.existsByEmail(user.email())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Этот email уже используется"));
            }

            if (usersDAO.existsByPhoneNumber(user.phoneNumber())) {
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

            usersDAO.save(user);
            Long userId = usersDAO.findByEmail(user.email()).userId();
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
            if (!usersDAO.existsByEmail(user.email())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }
            boolean updated = usersDAO.update(user);
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
            if (usersDAO.show(bio.userId()) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }

            System.out.println(bio);
            biosDAO.addBio(bio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать био пользователя: " + e.getMessage()));
        }
    }

    @GetMapping("/bio")
    public ResponseEntity<?> getBio(@RequestParam Long userId) {
        try {
            if (usersDAO.show(userId) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }

            var authenticatedUser = securityService.getAuthenticatedUser();
            var bio = biosDAO.getBioByUserId(userId);
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
            if (usersDAO.show(bio.userId()) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Пользователь не найден"));
            }

            bio.updatedAt(LocalDateTime.now());
            boolean updated = biosDAO.updateBio(bio);
            if (updated) {
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.badRequest().body("Произошла ошибка при обновлении данных пользователя");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Не удалось создать пользователя: " + e.getMessage()));
        }
    }
}
