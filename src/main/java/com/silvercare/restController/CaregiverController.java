package com.silvercare.restController;

import com.silvercare.models.Caregiver;
import com.silvercare.models.CaregiverSummary;
import com.silvercare.services.CaregiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/caregivers")
public class CaregiverController {

    @Autowired
    private CaregiverService caregiverService;

    @GetMapping
    public List<Caregiver> listAll() {
        return caregiverService.findAll();
    }

    @GetMapping("/merged")
    public List<CaregiverSummary> mergedList() {
        return caregiverService.getMergedCaregiverSummaries();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Caregiver> getById(@PathVariable Integer id) {
        Optional<Caregiver> c = caregiverService.findById(id);
        return c.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Caregiver> create(@RequestBody Caregiver caregiver) {
        Caregiver saved = caregiverService.save(caregiver);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Caregiver> update(@PathVariable Integer id, @RequestBody Caregiver payload) {
        Optional<Caregiver> existing = caregiverService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        Caregiver e = existing.get();
        e.setName(payload.getName());
        e.setQualifications(payload.getQualifications());
        e.setImagePath(payload.getImagePath());
        e.setAvailabilityStatus(payload.getAvailabilityStatus());
        caregiverService.save(e);
        return ResponseEntity.ok(e);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        caregiverService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Caregiver> search(@RequestParam(value = "q", required = false) String q,
                                  @RequestParam(value = "status", required = false) String status) {
        if (q != null && !q.isEmpty()) return caregiverService.searchByName(q);
        if (status != null && !status.isEmpty()) return caregiverService.findByAvailabilityStatus(status);
        return caregiverService.findAll();
    }
}