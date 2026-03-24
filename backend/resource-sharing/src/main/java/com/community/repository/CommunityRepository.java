package com.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.community.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {
}
