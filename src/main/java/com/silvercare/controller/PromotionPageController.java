package com.silvercare.controller;

import com.silvercare.models.Promotion;
import com.silvercare.repositories.PromotionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
public class PromotionPageController {
    private final PromotionRepository promotionRepository;

    public PromotionPageController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("/promotions/{id}")
    public String showPromotion(@PathVariable("id") Integer id, Model model) {
        Optional<Promotion> opt = promotionRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/"; // not found -> home
        }
        Promotion p = opt.get();
        model.addAttribute("promotion", p);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        model.addAttribute("startsAt", p.getStart_date() != null ? p.getStart_date().format(fmt) : null);
        model.addAttribute("endsAt", p.getEnd_date() != null ? p.getEnd_date().format(fmt) : null);
        return "/WEB-INF/jsp/public/promotion_detail.jsp";
    }
}
