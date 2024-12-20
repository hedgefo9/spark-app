package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.MatchesDAO;
import com.hedgefo9.spark.dao.UsersDAO;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Controller
@RequestMapping("/chat")
public class ChatController {

    SecurityService securityService;
    UsersDAO usersDAO;
    MatchesDAO matchesDAO;

    @Autowired
    public ChatController(SecurityService securityService, UsersDAO usersDAO, MatchesDAO matchesDAO) {
        this.securityService = securityService;
        this.usersDAO = usersDAO;
        this.matchesDAO = matchesDAO;
    }

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

        if (matchesDAO.check(realUser.userId(), receiverId)) {
            model.addAttribute("authedUser", realUser);
            model.addAttribute("receiver", usersDAO.show(receiverId));
        }

        return "chat";
    }

}
