package com.zerobase.web;

import com.zerobase.model.ScrapedResult;
import com.zerobase.service.FinanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/finance")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/dividend/{companyName}")
    public ResponseEntity<?> searchFinance(@PathVariable String companyName) {
        ScrapedResult result = this.financeService.getDividendByCompanyName(companyName);
        return ResponseEntity.ok(result);
    }
}
