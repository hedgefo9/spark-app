package com.hedgefo9.spark.service;

import com.hedgefo9.spark.repository.AdminRepository;
import com.hedgefo9.spark.models.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final AdminRepository adminRepository;

	public List<Admin> findAll() {
		return adminRepository.findAll();
	}

	public Admin findById(int id) {
		return adminRepository.findById(id);
	}

	public Admin findByEmail(String email) {
		return adminRepository.findByEmail(email);
	}

	@Transactional
	public void save(Admin admin) {
		adminRepository.save(admin);
	}

	@Transactional
	public void deleteById(int id) {
		adminRepository.deleteById(id);
	}
}
