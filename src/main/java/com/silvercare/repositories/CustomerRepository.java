package com.silvercare.repositories;

import com.silvercare.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // This is the only "extra" method you need to define manually
    Optional<Customer> findByEmailAndPassword(String email, String password);

    // Helper to lookup by email (used by partner API)
    Optional<Customer> findByEmail(String email);
    List<Customer> findByAddressContainingIgnoreCaseAndCarePreferencesContainingIgnoreCase(
            String address, String careNeeds);
    
}