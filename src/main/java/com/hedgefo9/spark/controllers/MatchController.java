package com.hedgefo9.spark.controllers;

import com.hedgefo9.spark.dao.MatchesDAO;
import com.hedgefo9.spark.models.Match;
import com.hedgefo9.spark.models.Person;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.services.security.CustomUserDetails;
import com.hedgefo9.spark.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/match")
public class MatchController {

    private final MatchesDAO matchesDAO;
    private final SecurityService securityService;

    @Autowired
    public MatchController(MatchesDAO matchesDAO, SecurityService securityService) {
        this.matchesDAO = matchesDAO;
        this.securityService = securityService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAll(@PathVariable("id") long id) {
        var authenticatedUser = securityService.getAuthenticatedUser();
        boolean isAuthenticated = authenticatedUser.isPresent();

        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Только аутентифицированный пользователь может совершать это действие");
        }

        Person person = authenticatedUser.get().person();
        User realUser = (User) person;

        if (!Objects.equals(id, realUser.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы можете просмотреть только свои мэтчи");
        }

        List<Match> matches = matchesDAO.getAllByUserId(id);
        if (!matches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(matches);
        }
        return ResponseEntity.badRequest().body("Вы не можете просмотреть свои мэтчи из-за какой-то ошибки");
    }
}
