package com.hedgefo9.spark.controller;

import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.repository.UserRepository;
import com.hedgefo9.spark.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserRepository userRepository;
    private final SecurityService securityService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", userRepository.findAll());

        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();
        model.addAttribute("isAuthenticated", securityService.getAuthenticatedUser().isPresent());

        if (isAuthenticated) {
            Person person = authenticatedUser.get().person();
            try {
                Admin admin = (Admin) person;
                return "/adminpanel";
            } catch (Exception e) {
                User realUser = (User) person;
                model.addAttribute("authedUser", realUser);
            }
        }
        return "index";
    }

    @GetMapping("/adminpanel")
    public String admin(Model model) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (isAuthenticated) {
            Person person = authenticatedUser.get().person();
            try {
                Admin admin = (Admin) person;
                model.addAttribute("users", userRepository.findAll());
                model.addAttribute("authedAdmin", admin);
                return "adminpanel";
            } catch (Exception e) {
                e.printStackTrace();
                return "/";
            }
        }

        return "error";
    }


}
