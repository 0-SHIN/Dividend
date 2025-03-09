package com.zerobase.scraper;


import com.zerobase.model.Company;
import com.zerobase.model.Dividend;
import com.zerobase.model.ScrapedResult;
import com.zerobase.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
    private static final String STATICS_URL = "https://finance.yahoo.com/quote/%s/history/?filter=div&frequency=1mo&period1=%d&period2=%d";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";

    private static final long START_TIME = 86400;

    @Override
    public ScrapedResult scrap(Company company) {
        ScrapedResult scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;
            String url = String.format(STATICS_URL, company.getTicker(), START_TIME, now);

            Connection connection = Jsoup.connect(url);
            Document document = connection.userAgent(USER_AGENT).timeout(10 * 1000).get();

            Elements parsingDivs = document.select("table[class=table yf-1jecxey noDl  hideOnPrint]");
            Element tableEle = parsingDivs.get(0);
            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();

            for (Element e : tbody.children()) {
                String txt = e.text();
                String[] splits = txt.split(" ");

                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDate.of(year, month, day), dividend));
            }
            scrapedResult.setDividends(dividends);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker);

        try {
            Document document = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10 * 1000).get();
            Element titleEle = document.getElementsByTag("h1").get(1);
            String title = titleEle.text().split("\\(")[0].trim();

            return new Company(ticker, title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
