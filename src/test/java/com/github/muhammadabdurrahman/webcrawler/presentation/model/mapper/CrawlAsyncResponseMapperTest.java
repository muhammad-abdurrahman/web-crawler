package com.github.muhammadabdurrahman.webcrawler.presentation.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult.Page;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlAsyncResponse;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CrawlAsyncResponseMapperTest {

  private CrawlAsyncResponseMapper underTest;

  @BeforeEach
  void setUp() {
    underTest = Mappers.getMapper(CrawlAsyncResponseMapper.class);
  }

  @Test
  void shouldMapBookToCrawlResponse() {
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
    CrawlAsyncResponse actual = underTest.map(expected);

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

}