package com.zerobase.scraper;

import com.zerobase.model.Company;
import com.zerobase.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);
}
