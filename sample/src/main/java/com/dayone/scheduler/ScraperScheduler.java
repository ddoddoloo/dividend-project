package com.dayone.scheduler;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

//    @Scheduled(fixedDelay = 1000)
//    public void test1() throws InterruptedException {
//        Thread.sleep(10000);
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 1 : " + LocalDateTime.now());
//    }
//
//    @Scheduled(fixedDelay = 1000)
//    public void test2() throws InterruptedException {
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : " + LocalDateTime.now());
//    }

    // allEntries = true 로 설정하면 value 를 prefix 로 갖는 키를 모두 삭제함.
    // 특정 키만 삭제하고 싶으면 allEntries 대신 key 를 사용.
    // 스케쥴러 동작할때 마다 CacheEvict 동작.
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        // 저장된 회사 목록을 조회
        // 벌크 데이터를 가져오는 것이 무리가 될 수 있기 때문에, 스프링 배치를 사용하면 처리 가능 (작업처리 통계, 로깅 처리 등 가능)
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                    new Company(company.getTicker(), company.getName()));

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividendEntities().stream()
                    // 디비든 모델을 디비든 엔티티로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 하나씩 디비든 레파지토리에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }
}