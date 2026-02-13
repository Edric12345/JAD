package com.silvercare.services;

import com.silvercare.models.Promotion;
import com.silvercare.repositories.PromotionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepo;

    public PromotionService(PromotionRepository promotionRepo) {
        this.promotionRepo = promotionRepo;
    }

    @Cacheable("activePromotions")
    public List<Promotion> getActivePromotions(String target) {
        return promotionRepo.findActiveByTarget(target, LocalDateTime.now());
    }
}
