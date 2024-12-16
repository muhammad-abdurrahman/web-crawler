package com.github.muhammadabdurrahman.webcrawler.persistence.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult.Page;
import com.github.muhammadabdurrahman.webcrawler.persistence.model.CrawlResultEntity;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CrawlResultEntityMapperTest {

  private CrawlResultEntityMapper underTest;

  @BeforeEach
  void setUp() {
    underTest = Mappers.getMapper(CrawlResultEntityMapper.class);
  }

  @Test
  void shouldMapCrawlResultEntityToCrawlResult() {
    // given
    var expected = CrawlResultEntity.builder()
        .startingUrl("startingUrl")
        .visitedPages(
            Set.of(
                CrawlResultEntity.Page.builder()
                    .pageUrl("pageUrl")
                    .hyperlinks(Set.of("link"))
                    .build()
            )
        )
        .build();

    // when
    CrawlResult actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFields("ttl", "createdAt")
        .isEqualTo(expected);
  }

  @Test
  void shouldMapCrawlResultToCrawlResultEntity() {
    // given
    var expected = CrawlResult.builder()
        .startingUrl("startingUrl")
        .visitedPages(
            Set.of(
                Page.builder()
                    .pageUrl("pageUrl")
                    .hyperlinks(Set.of("link"))
                    .build()
            )
        )
        .build();

    // when
    CrawlResultEntity actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFields("ttl", "createdAt")
        .isEqualTo(expected);
  }
}