package com.zerobase.service;

import com.zerobase.exception.impl.AlreadyExistTickerException;
import com.zerobase.exception.impl.NoCompanyException;
import com.zerobase.model.Company;
import com.zerobase.model.ScrapedResult;
import com.zerobase.persist.CompanyRepository;
import com.zerobase.persist.DividendRepository;
import com.zerobase.persist.entity.CompanyEntity;
import com.zerobase.persist.entity.DividendEntity;
import com.zerobase.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new AlreadyExistTickerException();
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);

        if (ObjectUtils.isEmpty(company)) {
            throw new NoCompanyException();
        }

        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().limit(10).collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        CompanyEntity company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName());

        return company.getName();
    }
}
