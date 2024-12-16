package com.github.muhammadabdurrahman.webcrawler.persistence.schedule;

import com.github.muhammadabdurrahman.webcrawler.business.store.CrawlResultStore;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlResultCacheEvictionScheduledTask {

  private final CrawlResultStore crawlResultStore;

  @Scheduled(cron = "${cache.eviction.cron}")
  public void scheduledEviction() {
    crawlResultStore.evictStaleCrawlResults();
  }
}
