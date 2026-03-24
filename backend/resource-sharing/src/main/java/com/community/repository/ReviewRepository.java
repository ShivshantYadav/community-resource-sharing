package com.community.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.community.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookingResourceResourceId(Long resourceId);
}
