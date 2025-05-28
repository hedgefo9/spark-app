package com.hedgefo9.spark.controller;

import com.hedgefo9.spark.models.Message;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.service.MessageService;
import com.hedgefo9.spark.service.security.CustomUserDetails;
import com.hedgefo9.spark.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final SecurityService securityService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/message")
    public ResponseEntity<?> getMessagesInDialogue(@RequestParam("receiverId") Long receiverId) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете получить сообщения в этом диалоге");
        }

        Person person = authenticatedUser.get().person();
        User realUser = (User) person;
        Long authedId = realUser.userId();
        var messages = messageService.getMessagesInDialogue(authedId, receiverId);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/message/{receiverId}")
    public void sendMessage(@DestinationVariable Long receiverId, @Payload Message message, Principal principal) throws IllegalAccessException {
        Long authenticatedUserId = getAuthenticatedUserId(principal);

        if (!Objects.equals(message.senderId(), authenticatedUserId)) {
            throw new IllegalArgumentException("User is not allowed to send this message");
        }

        messageService.sendMessage(message);
        simpMessagingTemplate.convertAndSend(
                "/topic/message/" + receiverId.toString(),
                message
        );
    }

    private Long getAuthenticatedUserId(Principal principal) throws IllegalAccessException {
        if (principal == null) {
            throw new IllegalAccessException("Unauthorized access");
        }

        CustomUserDetails userDetails = null;
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            userDetails = (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        }

        if (userDetails == null) {
            throw new IllegalAccessException("Invalid user details");
        }

        Person person = userDetails.person();
        User realUser = (User) person;
        return realUser.userId();
    }
}
