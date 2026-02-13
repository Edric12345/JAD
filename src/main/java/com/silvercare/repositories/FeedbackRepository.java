package com.silvercare.repositories;

import com.silvercare.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Using @Query stops Spring from panicking about the underscore in 'created_at'
    @Query("SELECT f FROM Feedback f ORDER BY f.created_at DESC")
    List<Feedback> findAllFeedbackSorted();

    // Average rating per service (service_id, avgRating)
    @Query("SELECT f.service, AVG(f.rating) as avgRating FROM Feedback f GROUP BY f.service ORDER BY avgRating DESC")
    List<Object[]> findTopRatedServices();

    @Query("SELECT f.service, AVG(f.rating) as avgRating FROM Feedback f GROUP BY f.service ORDER BY avgRating ASC")
    List<Object[]> findLowestRatedServices();
}