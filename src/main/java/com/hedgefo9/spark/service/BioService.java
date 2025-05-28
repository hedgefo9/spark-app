package com.hedgefo9.spark.service;

import com.hedgefo9.spark.models.Bio;
import com.hedgefo9.spark.repository.BioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BioService {
	private final BioRepository bioRepository;

	@Transactional
	public void addBio(Bio bio) {
		bioRepository.save(bio);
	}

	@Transactional
	public boolean updateBio(Bio bio) {
		return bioRepository.update(bio);
	}

	public Bio getBioByUserId(Long userId) {
		return bioRepository.findByUserId(userId);
	}
}