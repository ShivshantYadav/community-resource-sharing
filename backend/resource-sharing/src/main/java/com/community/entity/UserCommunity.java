package com.community.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_communities")
public class UserCommunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // ✅ User.userId is Long

    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;

    public UserCommunity() {}

    public UserCommunity(User user, Community community) {
        this.user = user;
        this.community = community;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }
}
