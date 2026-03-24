package com.community.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.community.entity.ReturnEntity;

public interface ReturnRepository extends JpaRepository<ReturnEntity, Long> {

	Optional<ReturnEntity> findByBookingBookingId(Long bookingId);
}
