package com.github.muhammadabdurrahman.webcrawler.persistence.store;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.store.CrawlResultStore;
import com.github.muhammadabdurrahman.webcrawler.concurrency.annotation.LockKey;
import com.github.muhammadabdurrahman.webcrawler.concurrency.annotation.SynchronizedByReentrantLock;
import com.github.muhammadabdurrahman.webcrawler.configuration.cache.CacheProperties;
import com.github.muhammadabdurrahman.webcrawler.persistence.model.CrawlResultEntity;
import com.github.muhammadabdurrahman.webcrawler.persistence.model.mapper.CrawlResultEntityMapper;
import com.github.muhammadabdurrahman.webcrawler.persistence.repository.CrawlResultJpaRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CrawlResultStoreImpl implements CrawlResultStore {

  private final CrawlResultJpaRepository repository;
  private final CrawlResultEntityMapper mapper;
  private final Clock clock;
  private final CacheProperties cacheProperties;

  @Override
  public Optional<CrawlResult> get(String startingUrl) {
    return repository.findById(startingUrl).map(mapper::map);
  }

  @SynchronizedByReentrantLock
  @Override
  public void refreshTtl(@LockKey String startingUrl) {
    repository.findByIdForUpdate(startingUrl).ifPresent(
        entity -> {
          entity.setTtl(clock.millis() + cacheProperties.getTtl().toMillis());
          repository.saveAndFlush(entity);
        }
    );
  }

  @SynchronizedByReentrantLock
  @Override
  public void put(@LockKey CrawlResult crawlResult) {
    if (repository.existsById(crawlResult.getStartingUrl())) {
      return;
    }

    CrawlResultEntity entity = mapper.map(crawlResult)
        .withTtl(clock.millis() + cacheProperties.getTtl().toMillis());
    repository.saveAndFlush(entity);
  }

  @Override
  public void evictStaleCrawlResults() {
    repository.deleteByTtlLessThanOrCreatedAtBefore(
        clock.millis(),
        LocalDateTime.now(clock).minus(cacheProperties.getRetentionDuration())
    );
  }
}
