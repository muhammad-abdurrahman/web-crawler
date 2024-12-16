package com.github.muhammadabdurrahman.webcrawler.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "crawl_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "startingUrl")
public class CrawlResultEntity {

  @Id
  @Column(name = "starting_url", nullable = false)
  private String startingUrl;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "visited_pages", columnDefinition = "json")
  private Set<Page> visitedPages;

  @With
  @Column(name = "ttl")
  private Long ttl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @EqualsAndHashCode(of = "pageUrl")
  public static class Page {

    private String pageUrl;
    private Set<String> hyperlinks;
  }
}