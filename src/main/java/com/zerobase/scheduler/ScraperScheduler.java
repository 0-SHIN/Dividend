package com.zerobase.scheduler;

import com.zerobase.model.Company;
import com.zerobase.model.ScrapedResult;
import com.zerobase.model.constants.CacheKey;
import com.zerobase.persist.CompanyRepository;
import com.zerobase.persist.DividendRepository;
import com.zerobase.persist.entity.CompanyEntity;
import com.zerobase.persist.entity.DividendEntity;
import com.zerobase.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@EnableCaching
@Component
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        List<CompanyEntity> companies = this.companyRepository.findAll();

        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started -> {}", company.getName());
            ScrapedResult scrapedResult =
                    this.yahooFinanceScraper.scrap(new Company(company.getTicker(), company.getName()));

            scrapedResult.getDividends().stream()
                    .map(e -> new DividendEntity(company.getId(), e))
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> {}", e.toString());
                        }
                    });

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }


    }
}
