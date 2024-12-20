package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.UserPhotosDAO;
import com.hedgefo9.spark.models.UserPhoto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/photos")
public class UserPhotoController {

    private final UserPhotosDAO userPhotosDAO;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Autowired
    public UserPhotoController(UserPhotosDAO userPhotosDAO) {
        this.userPhotosDAO = userPhotosDAO;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserPhoto>> getPhotosByUserId(@PathVariable Long userId) {
        List<UserPhoto> photos = userPhotosDAO.findByUserId(userId);
        return ResponseEntity.ok(photos);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPhoto(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", required = false, defaultValue = "false") boolean isPrimary) {


        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл отсутствует!");
        }


        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String generatedFileName = UUID.randomUUID() + fileExtension;


        try {
            File saveFile = Paths.get(uploadDir, generatedFileName).toFile();
            file.transferTo(saveFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Ошибка при сохранении файла: " + e.getMessage());
        }


        UserPhoto userPhoto = new UserPhoto();
        userPhoto.userId(userId);
        userPhoto.fileName(generatedFileName);
        userPhoto.isPrimary(isPrimary);

        userPhotosDAO.addPhoto(userPhoto);

        return ResponseEntity.ok("Фотография успешно загружена!");
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {

        Optional<UserPhoto> optionalPhoto = userPhotosDAO.findById(photoId);
        if (optionalPhoto.isPresent()) {
            UserPhoto photo = optionalPhoto.get();


            File fileToDelete = Paths.get(uploadDir, photo.fileName()).toFile();
            if (fileToDelete.exists() && !fileToDelete.delete()) {
                return ResponseEntity.internalServerError().build();
            }


            userPhotosDAO.deletePhoto(photoId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/primary")
    public ResponseEntity<UserPhoto> getPrimaryPhotoByUserId(@PathVariable Long userId) {
        Optional<UserPhoto> primaryPhoto = userPhotosDAO.findPrimaryPhotoByUserId(userId);
        return primaryPhoto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/primary/{photoId}")
    public ResponseEntity<Void> setPrimaryPhoto(@PathVariable Long userId, @PathVariable Long photoId) {
        userPhotosDAO.setPrimaryPhoto(userId, photoId);
        return ResponseEntity.ok().build();
    }
}
