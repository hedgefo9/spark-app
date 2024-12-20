package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.UsersDAO;
import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    UsersDAO usersDAO;
    SecurityService securityService;

    @Autowired
    public HomeController(UsersDAO usersDAO, SecurityService securityService) {
        this.usersDAO = usersDAO;
        this.securityService = securityService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", usersDAO.index());

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
                model.addAttribute("users", usersDAO.index());
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
