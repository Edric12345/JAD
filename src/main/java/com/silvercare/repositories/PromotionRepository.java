package com.silvercare.repositories;

import com.silvercare.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion,Integer> {
    @Query("select p from Promotion p where p.is_published = true and (p.start_date is null or p.start_date <= :now) and (p.end_date is null or p.end_date >= :now) and p.target = :target order by p.priority desc")
    List<Promotion> findActiveByTarget(@Param("target") String target, @Param("now") LocalDateTime now);
}
