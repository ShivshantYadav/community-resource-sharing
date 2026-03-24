package com.community.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.community.entity.User;
import com.community.service.UserService;

@Component
public class SecurityUtil {

    @Autowired
    private UserService userService;

    public User getLoggedInUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userService.getByEmail(email);
    }
}
