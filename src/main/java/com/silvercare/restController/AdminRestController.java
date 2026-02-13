package com.silvercare.restController;

import com.silvercare.models.CareService;
import com.silvercare.models.ServiceCategory;
import com.silvercare.repositories.ServiceRepository;
import com.silvercare.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin") // REST endpoints for admin
public class AdminRestController {

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    // --- GET all services (optional category filter) ---
    @GetMapping("/services")
    public List<CareService> getAllServices(@RequestParam(required = false) Integer categoryId) {
        if (categoryId != null) {
            return serviceRepo.findByCategoryId(categoryId);
        }
        return serviceRepo.findAll();
    }

    // --- GET single service ---
    @GetMapping("/services/{id}")
    public CareService getService(@PathVariable int id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }

    // --- CREATE new service ---
    @PostMapping("/services")
    public CareService createService(@RequestBody CareService service) {
        return serviceRepo.save(service);
    }

    // --- UPDATE existing service ---
    @PutMapping("/services/{id}")
    public CareService updateService(@PathVariable int id, @RequestBody CareService service) {
        CareService existing = serviceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        existing.setService_name(service.getService_name());
        existing.setDescription(service.getDescription());
        existing.setPrice(service.getPrice());
        existing.setAvailability(service.getAvailability());
        if (service.getCategory() != null) existing.setCategory(service.getCategory());

        return serviceRepo.save(existing);
    }

    // --- DELETE service ---
    @DeleteMapping("/services/{id}")
    public String deleteService(@PathVariable int id) {
        serviceRepo.deleteById(id);
        return "Service deleted successfully";
    }

    // --- GET all categories ---
    @GetMapping("/categories")
    public List<ServiceCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    // --- GET single category ---
    @GetMapping("/categories/{id}")
    public ServiceCategory getCategory(@PathVariable int id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // --- CREATE category ---
    @PostMapping("/categories")
    public ServiceCategory createCategory(@RequestBody ServiceCategory category) {
        return categoryRepo.save(category);
    }

    // --- UPDATE category ---
    @PutMapping("/categories/{id}")
    public ServiceCategory updateCategory(@PathVariable int id, @RequestBody ServiceCategory category) {
        ServiceCategory existing = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existing.setCategory_name(category.getCategory_name());
        existing.setDescription(category.getDescription());

        return categoryRepo.save(existing);
    }

    // --- DELETE category ---
    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable int id) {
        categoryRepo.deleteById(id);
        return "Category deleted successfully";
    }
}
