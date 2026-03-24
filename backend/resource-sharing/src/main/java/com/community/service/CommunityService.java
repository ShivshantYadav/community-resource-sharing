package com.community.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.community.entity.Community;
import com.community.entity.User;
import com.community.entity.UserCommunity;
import com.community.repository.CommunityRepository;
import com.community.repository.UserCommunityRepository;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserCommunityRepository userCommunityRepository;

    // Join a community
    public void joinCommunity(User user, Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));

        // Check if already joined
        List<UserCommunity> existing = userCommunityRepository.findByUserUserId(user.getUserId());
        boolean alreadyJoined = existing.stream()
                .anyMatch(uc -> uc.getCommunity().getCommunityId().equals(communityId));
        if (alreadyJoined) {
            throw new RuntimeException("User already joined this community");
        }

        UserCommunity userCommunity = new UserCommunity();
        userCommunity.setUser(user);
        userCommunity.setCommunity(community);
        userCommunityRepository.save(userCommunity);
    }

    // Get communities of a user
    public List<UserCommunity> getUserCommunities(Long userId) {
        return userCommunityRepository.findByUserUserId(userId);
    }
}
