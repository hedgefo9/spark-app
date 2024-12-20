package com.hedgefo9.spark.services.security;

import com.hedgefo9.spark.dao.AdminsDAO;
import com.hedgefo9.spark.dao.UsersDAO;
import com.hedgefo9.spark.models.Admin;
import com.hedgefo9.spark.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminsDAO adminsDAO;
    private final UsersDAO usersDAO;

    @Autowired
    public CustomUserDetailsService(AdminsDAO adminsDAO, UsersDAO usersDAO) {
        this.adminsDAO = adminsDAO;
        this.usersDAO = usersDAO;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = null;
        try {
           admin = adminsDAO.findByEmail(username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (admin != null) {
            return new CustomUserDetails(admin);
        }

        User user = usersDAO.findByEmail(username);
        return new CustomUserDetails(user);

    }
}
