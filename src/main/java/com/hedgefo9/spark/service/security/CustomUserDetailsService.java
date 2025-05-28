package com.hedgefo9.spark.service.security;

import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.User;
import com.hedgefo9.spark.repository.UserRepository;
import com.hedgefo9.spark.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminService adminService;
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = null;
        try {
            admin = adminService.findByEmail(username);
        } catch (Exception _) {
        }

        if (admin != null) {
            return new CustomUserDetails(admin);
        }

        User user = userRepository.findByEmail(username);
        return new CustomUserDetails(user);

    }
}
