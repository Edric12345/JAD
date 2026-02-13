package com.silvercare.services;

import com.silvercare.models.Caregiver;
import com.silvercare.models.CaregiverBooking;
import com.silvercare.models.CaregiverSummary;
import com.silvercare.repositories.CaregiverBookingRepository;
import com.silvercare.repositories.CaregiverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CaregiverService {

    @Autowired
    private CaregiverRepository caregiverRepository;

    @Autowired
    private CaregiverBookingRepository caregiverBookingRepository;

    public List<Caregiver> findAll() {
        return caregiverRepository.findAll();
    }

    public Optional<Caregiver> findById(Integer id) {
        return caregiverRepository.findById(id);
    }

    public Caregiver save(Caregiver caregiver) {
        return caregiverRepository.save(caregiver);
    }

    public void deleteById(Integer id) {
        caregiverRepository.deleteById(id);
    }

    public List<Caregiver> findByAvailabilityStatus(String status) {
        return caregiverRepository.findByAvailabilityStatus(status);
    }

    public List<Caregiver> searchByName(String keyword) {
        return caregiverRepository.findByNameContainingIgnoreCase(keyword);
    }

    // New: produce a merged summary list where caregiver_bookings snapshot values,
    // if present, override the live Caregiver values. This supports legacy data loads.
    public List<CaregiverSummary> getMergedCaregiverSummaries() {
        List<Caregiver> live = caregiverRepository.findAll();
        List<CaregiverBooking> snapshots = caregiverBookingRepository.findAll();

        Map<String, CaregiverBooking> snapshotByName = new HashMap<>();
        for (CaregiverBooking cb : snapshots) {
            if (cb.getName() != null && !cb.getName().isBlank()) snapshotByName.put(cb.getName().trim(), cb);
        }

        List<CaregiverSummary> out = new ArrayList<>();
        for (Caregiver c : live) {
            CaregiverBooking cb = snapshotByName.getOrDefault(c.getName(), null);
            String name = c.getName();
            String qualifications = c.getQualifications();
            String imagePath = c.getImagePath();
            String status = c.getAvailabilityStatus();
            if (cb != null) {
                if (cb.getName() != null && !cb.getName().isBlank()) name = cb.getName();
                if (cb.getQualifications() != null && !cb.getQualifications().isBlank()) qualifications = cb.getQualifications();
                if (cb.getImagePath() != null && !cb.getImagePath().isBlank()) imagePath = cb.getImagePath();
                if (cb.getAvailabilityStatus() != null && !cb.getAvailabilityStatus().isBlank()) status = cb.getAvailabilityStatus();
            }
            out.add(new CaregiverSummary(c.getId(), name, qualifications, imagePath, status));
        }

        // Also include any snapshot-only caregivers (no live entry) so data from CSV/SQL is visible
        for (CaregiverBooking cb : snapshots) {
            boolean exists = false;
            for (Caregiver c : live) if (c.getName() != null && c.getName().equals(cb.getName())) { exists = true; break; }
            if (!exists) {
                out.add(new CaregiverSummary(null, cb.getName(), cb.getQualifications(), cb.getImagePath(), cb.getAvailabilityStatus()));
            }
        }

        return out;
    }
}