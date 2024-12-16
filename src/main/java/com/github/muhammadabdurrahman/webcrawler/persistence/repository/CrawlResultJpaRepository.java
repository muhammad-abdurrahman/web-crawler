package com.github.muhammadabdurrahman.webcrawler.persistence.repository;

import com.github.muhammadabdurrahman.webcrawler.persistence.model.CrawlResultEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CrawlResultJpaRepository extends JpaRepository<CrawlResultEntity, String> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM CrawlResultEntity c WHERE c.startingUrl = :startingUrl")
  Optional<CrawlResultEntity> findByIdForUpdate(@Param("startingUrl") String startingUrl);

  @Transactional
  @Modifying
  void deleteByTtlLessThanOrCreatedAtBefore(long ttl, LocalDateTime dateTime);
}
