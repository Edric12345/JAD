package com.silvercare.repositories;

import com.silvercare.models.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;



public interface CategoryRepository extends JpaRepository<ServiceCategory, Integer> {

    @Query("SELECT c FROM ServiceCategory c WHERE LOWER(c.category_name) = LOWER(:name)")
    Optional<ServiceCategory> findByNameIgnoreCase(@Param("name") String name);
  

}