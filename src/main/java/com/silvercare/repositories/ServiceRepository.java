package com.silvercare.repositories;

import com.silvercare.models.CareService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<CareService, Integer> {
	@Query("SELECT s FROM CareService s WHERE s.category_id = :catId ORDER BY s.service_name ASC")
	List<CareService> findByCategoryId(@Param("catId") int catId);

	// Find services with low availability (e.g., availability is not null and <= threshold)
	@Query("SELECT s FROM CareService s WHERE s.availability IS NOT NULL AND s.availability <= :threshold ORDER BY s.availability ASC")
	List<CareService> findLowAvailability(@Param("threshold") int threshold);

	// Explicit JPQL search method to avoid property-path parsing issues for underscored field names
	@Query("SELECT s FROM CareService s WHERE LOWER(s.service_name) LIKE LOWER(CONCAT('%', :namePart, '%')) ORDER BY s.service_name ASC")
	List<CareService> searchByName(@Param("namePart") String namePart);
}