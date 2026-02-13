package com.silvercare.restController;

import com.silvercare.models.CareService;
import com.silvercare.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/external") // B2B API path
public class ServiceRestController {

    @Autowired
    private ServiceRepository serviceRepo;

    // 1️⃣ Get all services
    @GetMapping("/services")
    public List<CareService> getAllServicesForPartners(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer lowAvailabilityThreshold,
            @RequestParam(required = false) String searchName
    ) {
        if (categoryId != null) {
            // Services filtered by category
            return serviceRepo.findByCategoryId(categoryId);
        } else if (lowAvailabilityThreshold != null) {
            // Services with low availability
            return serviceRepo.findLowAvailability(lowAvailabilityThreshold);
        } else if (searchName != null && !searchName.isEmpty()) {
            // Search by name
            return serviceRepo.searchByName(searchName);
        } else {
            // Return all services
            return serviceRepo.findAll();
        }
    }

    // 2️⃣ Optional: single service by ID
    @GetMapping("/services/{id}")
    public CareService getServiceById(@PathVariable int id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }
}
