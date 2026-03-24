package com.community.repository;

import com.community.entity.Booking;
import com.community.entity.BookingStatus;
import com.community.entity.Resource;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    // ================= OWNER BOOKINGS =================
    @Query("""
        SELECT DISTINCT b FROM Booking b
        JOIN FETCH b.borrower
        JOIN FETCH b.resource r
        JOIN FETCH r.owner
        WHERE r.owner.userId = :ownerId
    """)
    List<Booking> findByResourceOwner(@Param("ownerId") Long ownerId);

    // ================= OWNER BOOKINGS BY SINGLE STATUS =================
    @Query("""
        SELECT DISTINCT b FROM Booking b
        JOIN FETCH b.borrower
        JOIN FETCH b.resource r
        JOIN FETCH r.owner
        WHERE r.owner.userId = :ownerId
          AND b.bookingStatus = :status
    """)
    List<Booking> findByResourceOwnerAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status
    );

    // ================= OWNER BOOKINGS BY MULTIPLE STATUSES =================
    @Query("""
        SELECT DISTINCT b FROM Booking b
        JOIN FETCH b.borrower
        JOIN FETCH b.resource r
        JOIN FETCH r.owner
        WHERE r.owner.userId = :ownerId
          AND b.bookingStatus IN :statuses
    """)
    List<Booking> findByResourceOwnerAndBookingStatusIn(
            @Param("ownerId") Long ownerId,
            @Param("statuses") List<BookingStatus> statuses
    );

    // ================= USER BOOKINGS =================
    @Query("""
        SELECT DISTINCT b FROM Booking b
        JOIN FETCH b.resource r
        JOIN FETCH r.owner
        WHERE b.borrower.userId = :userId
    """)
    List<Booking> findByBorrowerWithResource(@Param("userId") Long userId);

    // ================= SINGLE BOOKING =================
    @Query("""
        SELECT DISTINCT b FROM Booking b
        JOIN FETCH b.borrower
        JOIN FETCH b.resource r
        JOIN FETCH r.owner
        WHERE b.bookingId = :id
    """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    // ================= ALL BOOKINGS FOR OWNER =================
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resource.owner.userId = :ownerId
    """)
    List<Booking> findAllByOwner(@Param("ownerId") Long ownerId);
    
    List<Booking> findByResource(Resource resource);
    
    @Transactional
    void deleteByResource(Resource resource);
}
