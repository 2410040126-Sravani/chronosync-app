package com.chronosync.controller;

import com.chronosync.model.VendorTodaySummary;
import com.chronosync.repository.VendorTodaySummaryRepository;
import com.chronosync.service.VendorPrecomputeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/vendor")
@CrossOrigin(origins = "http://localhost:5173")
public class VendorTodaySummaryController {

    private final VendorTodaySummaryRepository summaryRepo;
    private final VendorPrecomputeService precomputeService;

    public VendorTodaySummaryController(VendorTodaySummaryRepository summaryRepo,
                                        VendorPrecomputeService precomputeService) {
        this.summaryRepo = summaryRepo;
        this.precomputeService = precomputeService;
    }

    // ✅ FIXED: changed endpoint to avoid duplicate mapping
    @GetMapping("/{vendorId}/today-summary-fast")
    public VendorTodaySummary todaySummary(@PathVariable Long vendorId) {
        return summaryRepo
                .findByVendorIdAndSummaryDate(vendorId, LocalDate.now())
                .orElseGet(() -> precomputeService.recomputeForVendor(vendorId));
    }

    // ✅ demo button: "Recompute Now"
    @PostMapping("/{vendorId}/recompute-today")
    public VendorTodaySummary recomputeToday(@PathVariable Long vendorId) {
        return precomputeService.recomputeForVendor(vendorId);
    }
}