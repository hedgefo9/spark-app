package com.hedgefo9.spark.service;

import com.hedgefo9.spark.models.Message;
import com.hedgefo9.spark.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageRepository messageRepository;

	public List<Message> getMessagesInDialogue(Long userId1, Long userId2) {
		return messageRepository.findMessagesInDialogue(userId1, userId2);
	}

	@Transactional
	public boolean sendMessage(Message message) {
		return messageRepository.saveMessage(message);
	}
}