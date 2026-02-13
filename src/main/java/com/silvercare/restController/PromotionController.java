package com.silvercare.restController;

import com.silvercare.models.Promotion;
import com.silvercare.services.PromotionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/active")
    public List<Promotion> active(@RequestParam(defaultValue = "header") String target) {
        return promotionService.getActivePromotions(target);
    }
}
