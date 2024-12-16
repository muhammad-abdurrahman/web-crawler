package com.github.muhammadabdurrahman.webcrawler.persistence.schedule;

import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.webcrawler.persistence.store.CrawlResultStoreImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlResultCacheEvictionScheduledTaskTest {

  @Mock
  private CrawlResultStoreImpl crawlResultStore;

  @InjectMocks
  private CrawlResultCacheEvictionScheduledTask underTest;

  @Test
  void shouldEvictStaleCrawlResults() {
    // when
    underTest.scheduledEviction();

    // then
    verify(crawlResultStore).evictStaleCrawlResults();
  }
}