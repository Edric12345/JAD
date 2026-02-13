package com.silvercare.repositories;

import com.silvercare.models.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminUser, String> {
	//This repo is the JPAinterface/DAO for AdminUser model and
	//it uses hiberate to generate the SQL and
	//entityManager for utility/manage database sessions
   
	// This replaces "SELECT * FROM admin_user WHERE username=? AND password=?"
    Optional<AdminUser> findByUsernameAndPassword(String username, String password);
    
}