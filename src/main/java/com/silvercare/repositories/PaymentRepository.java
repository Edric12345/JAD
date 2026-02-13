package com.silvercare.repositories;

import com.silvercare.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Helper to fetch payment by booking id (convenience for receipt view)
    Optional<Payment> findByBooking_Id(int bookingId);

    // Find payments within a date/time range
    List<Payment> findByPaidAtBetween(LocalDateTime from, LocalDateTime to);

    // Top clients by total amount (returns customer, totalSum)
    @Query("SELECT p.customer, SUM(COALESCE(p.totalAmount, p.amount)) as total FROM Payment p GROUP BY p.customer ORDER BY total DESC")
    List<Object[]> findTopClientsBySpend();
}