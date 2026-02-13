package com.silvercare.repositories;

import com.silvercare.models.Booking;
import com.silvercare.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    // Fixed: Uses underscore to navigate into the Customer entity's ID
    List<Booking> findByCustomer_Id(int customerId);

    @Transactional
    @Modifying
    @Query("UPDATE Booking b SET b.status = 'CANCELLED' WHERE b.id = ?1 AND b.customer.id = ?2")
    int cancelBooking(int bookingId, int customerId);
    
    @Query("SELECT b FROM Booking b WHERE b.caregiver.id = :cid")
    List<Booking> findByCaregiverId(@Param("cid") int caregiverId);

    
    @Query("SELECT d.service, COUNT(d) as count FROM BookingDetails d GROUP BY d.service ORDER BY count DESC")
    List<Object[]> findHighDemandServices();

    @Query("SELECT DISTINCT b.customer FROM Booking b JOIN b.details d WHERE d.service.id = :serviceId")
    List<Customer> findCustomersByServiceId(int serviceId);

    @Query("SELECT b FROM Booking b WHERE b.booking_date BETWEEN :from AND :to ORDER BY b.booking_date ASC")
    List<Booking> findByBookingDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Count booking details that reference a specific service id. Used to guard delete operations.
    @Query("SELECT COUNT(d) FROM BookingDetails d WHERE d.service.id = :sid")
    long countDetailsByServiceId(@Param("sid") int sid);
}