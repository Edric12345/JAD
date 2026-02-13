package com.silvercare.restController;

import com.silvercare.models.ServiceCategory;
import com.silvercare.models.CareService;
import com.silvercare.repositories.CategoryRepository;
import com.silvercare.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/b2b")
@CrossOrigin(origins = "*") // Allow third-party clients to call this API; adjust for production
public class RestB2BController {

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    // Requirement: Retrieval of care service categories for healthcare providers or support services
    @GetMapping("/categories")
    public List<ServiceCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    // B2B: services by category (optional categoryId param)
    @GetMapping("/services")
    public List<CareService> getServicesByCategory(@RequestParam(required = false) Integer categoryId) {
        if (categoryId == null) return serviceRepo.findAll();
        return serviceRepo.findByCategoryId(categoryId);
    }

    // New: services by category name (friendly for third parties)
    @GetMapping("/services/by-name")
    public ResponseEntity<?> getServicesByCategoryName(@RequestParam(required = false) String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return ResponseEntity.badRequest().body("categoryName parameter required");
        }
        Optional<ServiceCategory> opt = categoryRepo.findByNameIgnoreCase(categoryName.trim());
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Category not found");
        List<CareService> list = serviceRepo.findByCategoryId(opt.get().getId());
        return ResponseEntity.ok(list);
    }

    // Convenience endpoint: cleaning services (common B2B case)
    @GetMapping("/services/cleaning")
    public List<CareService> getCleaningServices() {
        Optional<ServiceCategory> opt = categoryRepo.findByNameIgnoreCase("Cleaning");
        if (opt.isEmpty()) return List.of();
        return serviceRepo.findByCategoryId(opt.get().getId());
    }
}