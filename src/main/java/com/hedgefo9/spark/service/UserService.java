package com.hedgefo9.spark.service;

import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User getUserById(long id) {
		return userRepository.findById(id);
	}

	@Transactional
	public void createUser(User user) {
		userRepository.save(user);
	}

	@Transactional
	public boolean updateUser(User user) {
		return userRepository.update(user);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean existsByPhoneNumber(String phoneNumber) {
		return userRepository.existsByPhoneNumber(phoneNumber);
	}

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
}
