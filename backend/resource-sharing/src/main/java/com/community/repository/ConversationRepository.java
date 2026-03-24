package com.community.repository;

import com.community.entity.Booking;
import com.community.entity.Conversation;
import com.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	 @Query("""
		        select c from Conversation c
		        join fetch c.borrower
		        join fetch c.owner
		        where c.id = :id
		    """)
		    Optional<Conversation> findByIdWithUsers(@Param("id") Long id);
    @Query("""
        select c from Conversation c
        join fetch c.borrower
        join fetch c.owner
        where c.booking.id = :bookingId
    """)
    Optional<Conversation> findByBookingIdWithUsers(Long bookingId);

    @Query("""
        select c from Conversation c
        where c.borrower = :user or c.owner = :user
    """)
    List<Conversation> findAllByUser(User user);

    @Query("""
        select c from Conversation c
        where (c.borrower = :borrower and c.owner = :owner)
           or (c.borrower = :owner and c.owner = :borrower)
    """)
    Optional<Conversation> findBetweenUsers(User borrower, User owner);
    
    @Modifying
    @Query("DELETE FROM Conversation c WHERE c.booking = :booking")
    void deleteByBooking(@Param("booking") Booking booking);
}
