package com.silvercare.repositories;

import com.silvercare.models.CaregiverBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaregiverBookingRepository extends JpaRepository<CaregiverBooking, Integer> {
    List<CaregiverBooking> findByNameContainingIgnoreCase(String name);
}
