package com.github.muhammadabdurrahman.webcrawler.persistence.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.configuration.cache.CacheProperties;
import com.github.muhammadabdurrahman.webcrawler.persistence.model.CrawlResultEntity;
import com.github.muhammadabdurrahman.webcrawler.persistence.model.mapper.CrawlResultEntityMapper;
import com.github.muhammadabdurrahman.webcrawler.persistence.repository.CrawlResultJpaRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlResultStoreImplTest {

  @Mock
  private CrawlResultJpaRepository repository;

  @Mock
  private CrawlResultEntityMapper mapper;

  @Mock
  private Clock clock;

  @Mock
  private CacheProperties cacheProperties;

  @InjectMocks
  private CrawlResultStoreImpl underTest;

  private final String startingUrl = "http://example.com";
  private final long currentTimeMillis = 1622547800000L;

  @BeforeEach
  void setUp() {
    lenient().doReturn(Duration.ofSeconds(10)).when(cacheProperties).getTtl();
    lenient().doReturn(Duration.ofDays(7)).when(cacheProperties).getRetentionDuration();

    lenient().doReturn(currentTimeMillis).when(clock).millis();
    lenient().doReturn(Instant.ofEpochMilli(currentTimeMillis)).when(clock).instant();
    lenient().doReturn(ZoneOffset.UTC).when(clock).getZone();
  }

  @Test
  void shouldGetCrawlResult() {
    // given
    var entity = mock(CrawlResultEntity.class);
    var expectedCrawlResult = mock(CrawlResult.class);
    doReturn(Optional.of(entity)).when(repository).findById(startingUrl);
    doReturn(expectedCrawlResult).when(mapper).map(entity);

    // when
    Optional<CrawlResult> actualCrawlResult = underTest.get(startingUrl);

    // then
    assertThat(actualCrawlResult).contains(expectedCrawlResult);
  }

  @Test
  void shouldRefreshTtl() {
    // given
    var entity = mock(CrawlResultEntity.class);
    doReturn(Optional.of(entity)).when(repository).findByIdForUpdate(startingUrl);

    // when
    underTest.refreshTtl(startingUrl);

    // then
    verify(entity).setTtl(currentTimeMillis + cacheProperties.getTtl().toMillis());
    verify(repository).saveAndFlush(entity);
  }

  @Test
  void shouldPutCrawlResult() {
    // given
    var crawlResult = mock(CrawlResult.class);
    doReturn(startingUrl).when(crawlResult).getStartingUrl();
    doReturn(false).when(repository).existsById(startingUrl);
    var entity = mock(CrawlResultEntity.class);
    doReturn(entity).when(entity).withTtl(any());
    doReturn(entity).when(mapper).map(crawlResult);

    // when
    underTest.put(crawlResult);

    // then
    verify(entity).withTtl(currentTimeMillis + cacheProperties.getTtl().toMillis());
    verify(repository).saveAndFlush(entity);
  }

  @Test
  void shouldPutCrawlResultWhenAlreadyExists() {
    // given
    var crawlResult = mock(CrawlResult.class);
    doReturn(startingUrl).when(crawlResult).getStartingUrl();
    doReturn(true).when(repository).existsById(startingUrl);

    // when
    underTest.put(crawlResult);

    // then
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void shouldEvictStaleCrawlResults() {
    // when
    underTest.evictStaleCrawlResults();

    // then
    verify(repository).deleteByTtlLessThanOrCreatedAtBefore(
        currentTimeMillis,
        LocalDateTime.now(clock).minus(cacheProperties.getRetentionDuration())
    );
  }
}