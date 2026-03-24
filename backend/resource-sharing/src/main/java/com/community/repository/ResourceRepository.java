package com.community.repository;

import com.community.entity.Booking;
import com.community.entity.Resource;
import com.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

	@Query("""
		    select b from Booking b
		    join fetch b.resource
		    where b.bookingId = :id
		""")
		Optional<Booking> findWithResource(@Param("id") Long id);

    // Fetch resource by ID with owner + availability (avoid multiple bag fetch)
    @Query("SELECT r FROM Resource r " +
           "LEFT JOIN FETCH r.owner " +
           "LEFT JOIN FETCH r.availability " +
           "WHERE r.resourceId = :id")
    Optional<Resource> findByIdWithOwnerAndAvailability(@Param("id") Long id);

    // Fetch all resources for an owner (avoid fetching images in query)
    @Query("SELECT DISTINCT r FROM Resource r " +
           "LEFT JOIN FETCH r.owner " +
           "LEFT JOIN FETCH r.availability " +
           "WHERE r.owner = :owner")
    List<Resource> findByOwnerWithAvailability(@Param("owner") User owner);

    // Fetch all resources for public endpoint (avoid fetching images in query)
    @Query("SELECT DISTINCT r FROM Resource r " +
           "LEFT JOIN FETCH r.owner " +
           "LEFT JOIN FETCH r.availability")
    List<Resource> findAllWithOwnerAndAvailability();
}
