package com.community.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.community.entity.Conversation;
import com.community.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("""
    	    SELECT DISTINCT c FROM Conversation c
    	    JOIN FETCH c.booking b
    	    JOIN FETCH b.resource
    	    JOIN FETCH c.borrower
    	    JOIN FETCH c.owner
    	    WHERE c.borrower = :user OR c.owner = :user
    	    ORDER BY c.createdAt DESC
    	""")
    	List<Conversation> findAllByUserWithUsers(@Param("user") User user);

}
