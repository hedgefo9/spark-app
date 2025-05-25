package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.models.Like;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.services.LikeService;
import com.hedgefo9.spark.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/like")
public class LikeController {
    private final LikeService likeService;
    private final SecurityService securityService;

    @Autowired
    public LikeController(LikeService likeService, SecurityService securityService) {
        this.likeService = likeService;
        this.securityService = securityService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAllByUserId(@PathVariable("id") long id) {
        securityService.getAuthenticatedUser();
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете просмотреть только свои лайки");
        }

        Person person = authenticatedUser.get().person();
        User realUser = (User) person;
        if (id != realUser.userId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете просмотреть только свои лайки");
        }

        List<Like> likes = likeService.getAllBySenderId(id);
        if (!likes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(likes);
        }
        return ResponseEntity.badRequest().body("Вы не можете просмотреть свои лайки из-за какой-то ошибки");
    }

    @GetMapping
    public Boolean check(@RequestBody Like like) {
        if (Objects.equals(like.senderId(), like.receiverId())) {
            return false;
        }

        return likeService.check(like);
    }

    @PostMapping
    public ResponseEntity<String> add(@RequestBody Like like) {
        if (Objects.equals(like.senderId(), like.receiverId())) {
            return ResponseEntity.badRequest().body("Вы не можете лайкнуть сами себя");
        }

        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Только аутентифицированный пользователь может совершать это действие");
        }

        Person person = authenticatedUser.get().person();
        User realUser = (User) person;
        if (!Objects.equals(like.senderId(), realUser.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете просмотреть только свои лайки");
        }


        boolean created = likeService.add(like);
        if (created) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Лайк уже существует (или возникла другая ошибка)");
    }

    @DeleteMapping
    public ResponseEntity<String> remove(@RequestBody Like like) {
        if (Objects.equals(like.senderId(), like.receiverId())) {
            return ResponseEntity.badRequest().body("Вы не можете удалить лайк самому себе");
        }

        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Только аутентифицированный пользователь может совершать это действие");
        }

        Person person = authenticatedUser.get().person();
        User realUser = (User) person;
        if (!Objects.equals(like.senderId(), realUser.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете удалять только свой лайк");
        }

        boolean removed = likeService.remove(like);
        if (removed) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().body("Произошла ошибка при удалении лайка");
    }
}
