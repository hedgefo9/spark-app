package com.hedgefo9.spark.service;

import com.hedgefo9.spark.models.UserPhoto;
import com.hedgefo9.spark.repository.UserPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPhotoService {
	private final UserPhotoRepository userPhotoRepository;

	public List<UserPhoto> getPhotosByUserId(Long userId) {
		return userPhotoRepository.findByUserId(userId);
	}

	@Transactional
	public void addPhoto(UserPhoto userPhoto) {
		userPhotoRepository.addPhoto(userPhoto);
	}

	@Transactional
	public void deletePhoto(Long photoId) {
		userPhotoRepository.deletePhoto(photoId);
	}

	@Transactional
	public void setPrimaryPhoto(Long userId, Long photoId) {
		userPhotoRepository.setPrimaryPhoto(userId, photoId);
	}

	public Optional<UserPhoto> getPrimaryPhotoByUserId(Long userId) {
		return userPhotoRepository.findPrimaryPhotoByUserId(userId);
	}

	public Optional<UserPhoto> getPhotoById(Long photoId) {
		return userPhotoRepository.findById(photoId);
	}
}
