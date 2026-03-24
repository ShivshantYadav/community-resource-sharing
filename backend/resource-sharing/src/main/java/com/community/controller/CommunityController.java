package com.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.community.entity.User;
import com.community.service.CommunityService;
import com.community.service.UserService;

@RestController
@RequestMapping("/communities")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserService userService;

    // Join a community
    @PostMapping("/{communityId}/join")
    public String joinCommunity(@PathVariable Long communityId, @RequestParam String email) {
        // Get user by email (from JWT in real app, here using param for simplicity)
        User user = userService.getByEmail(email);
        communityService.joinCommunity(user, communityId);
        return "Joined successfully";
    }

    // Get all communities of a user
    @GetMapping("/my")
    public Object getMyCommunities(@RequestParam String email) {
        User user = userService.getByEmail(email);
        return communityService.getUserCommunities(user.getUserId());
    }
}
