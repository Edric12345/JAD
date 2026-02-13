package com.silvercare.repositories;

import com.silvercare.models.CustomerCart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerCartRepository extends JpaRepository<CustomerCart, Integer> {
    Optional<CustomerCart> findByCustomer_Id(int customerId);

    @Transactional
    void deleteByCustomer_Id(int customerId);
    
  
}