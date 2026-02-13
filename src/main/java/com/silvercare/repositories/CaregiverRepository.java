package com.silvercare.repositories;

import com.silvercare.models.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Import this
import java.util.List;

@Repository // Add this annotation
public interface CaregiverRepository extends JpaRepository<Caregiver, Integer> {

    List<Caregiver> findByAvailabilityStatus(String availabilityStatus);

    List<Caregiver> findByNameContainingIgnoreCase(String keyword);
}