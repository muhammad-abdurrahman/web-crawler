package com.github.muhammadabdurrahman.webcrawler.business.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult.Page;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CrawlResultLinkMapperTest {

  private CrawlResultLinkMapper underTest;

  @BeforeEach
  void setUp() {
    underTest = Mappers.getMapper(CrawlResultLinkMapper.class);
  }

  @Test
  void shouldMap() {
    // given
    var expected = new SimpleEntry<>("url", Set.of("link"));

    // when
    var actual = underTest.map(Map.ofEntries(expected));

    // then
    assertThat(actual)
        .containsExactly(
            Page.builder()
                .pageUrl(expected.getKey())
                .hyperlinks(expected.getValue())
                .build()
        );
  }
}