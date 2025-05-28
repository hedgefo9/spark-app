package com.hedgefo9.spark.controller;

import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.service.MatchService;
import com.hedgefo9.spark.service.UserService;
import com.hedgefo9.spark.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final MatchService matchService;
    private final SecurityService securityService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String chat(@PathVariable("id") Long receiverId, Model model) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();
        model.addAttribute("isAuthenticated", securityService.getAuthenticatedUser().isPresent());

        if (!isAuthenticated) {
            return "chat";
        }

        Person person = authenticatedUser.get().person();
        User realUser = (User) person;

        if (matchService.check(realUser.userId(), receiverId)) {
            model.addAttribute("authedUser", realUser);
            model.addAttribute("receiver", userService.getUserById(receiverId));
        }

        return "chat";
    }

}
