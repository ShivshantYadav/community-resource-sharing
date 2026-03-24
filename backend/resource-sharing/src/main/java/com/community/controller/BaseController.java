package com.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import com.community.entity.User;
import com.community.service.UserService;

public abstract class BaseController {

    @Autowired
    protected UserService userService;

    protected User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getByEmail(email);
    }
}
