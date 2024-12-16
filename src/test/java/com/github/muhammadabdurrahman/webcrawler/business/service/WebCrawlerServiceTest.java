package com.github.muhammadabdurrahman.webcrawler.business.service;

import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;

import com.github.muhammadabdurrahman.webcrawler.business.client.JsoupFacade;
import com.github.muhammadabdurrahman.webcrawler.business.client.RobotClient;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult.Page;
import com.github.muhammadabdurrahman.webcrawler.business.model.mapper.CrawlResultLinkMapper;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebCrawlerServiceTest {

  private static final String STARTING_URL = "https://example.com";

  @Mock
  private RobotClient robotClient;

  @Mock
  private JsoupFacade jsoupFacade;

  @Mock
  private CrawlResultLinkMapper mapper;

  @InjectMocks
  private WebCrawlerService underTest;

  private static URI startingUri;

  @BeforeAll
  static void beforeAll() {
    startingUri = URI.create(STARTING_URL);
  }

  @Test
  void shouldCrawlWithNoLinks() {
    // given
    doReturn(Set.of()).when(robotClient).getDisallowedPaths(startingUri);
    doReturn(Stream.empty()).when(jsoupFacade).getHyperlinks(STARTING_URL);
    doReturn(Set.of(
            Page.builder()
                .pageUrl(STARTING_URL)
                .hyperlinks(Set.of())
                .build()
        )
    ).when(mapper).map(Map.of(STARTING_URL, Set.of()));

    // when
    CrawlResult result = underTest.crawl(STARTING_URL);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.getStartingUrl()).isEqualTo(STARTING_URL);
      softly.assertThat(result.getVisitedPages())
          .hasSize(1)
          .extracting("pageUrl", "hyperlinks")
          .containsExactlyInAnyOrder(
              tuple(STARTING_URL, Set.of())
          );
    });
  }

  @Test
  void shouldCrawlWith1Link() {
    // given
    var page1Url = "https://example.com/page1";
    doReturn(Stream.of(page1Url)).when(jsoupFacade).getHyperlinks(STARTING_URL);
    doReturn(Set.of(
            Page.builder()
                .pageUrl(STARTING_URL)
                .hyperlinks(Set.of(page1Url))
                .build(),
            Page.builder()
                .pageUrl(page1Url)
                .hyperlinks(Set.of())
                .build()
        )
    ).when(mapper).map(
        Map.of(
            STARTING_URL, Set.of(page1Url),
            page1Url, Set.of()
        )
    );

    // when
    CrawlResult result = underTest.crawl(STARTING_URL);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.getStartingUrl()).isEqualTo(STARTING_URL);
      softly.assertThat(result.getVisitedPages())
          .hasSize(2)
          .extracting("pageUrl", "hyperlinks")
          .containsExactlyInAnyOrder(
              tuple(STARTING_URL, Set.of(page1Url)),
              tuple(page1Url, Set.of())
          );
    });
  }

  @Test
  void shouldCrawlWithManyLinks() {
    // given
    var page1Url = "https://example.com/page1";
    var page2Url = "https://example.com/page2";
    doReturn(Stream.of(page1Url, page2Url)).when(jsoupFacade).getHyperlinks(STARTING_URL);

    var doc2page1Url = "https://example.com/page2-1";
    var doc2page2Url = "https://example.com/page2-2";
    doReturn(Stream.of(doc2page1Url, doc2page2Url, STARTING_URL)).when(jsoupFacade).getHyperlinks(page1Url);

    doReturn(Set.of(
            Page.builder()
                .pageUrl(STARTING_URL)
                .hyperlinks(Set.of(page1Url, page2Url))
                .build(),
            Page.builder()
                .pageUrl(page1Url)
                .hyperlinks(Set.of(doc2page1Url, doc2page2Url, STARTING_URL))
                .build(),
            Page.builder()
                .pageUrl(page2Url)
                .hyperlinks(Set.of())
                .build(),
            Page.builder()
                .pageUrl(doc2page1Url)
                .hyperlinks(Set.of())
                .build(),
            Page.builder()
                .pageUrl(doc2page2Url)
                .hyperlinks(Set.of())
                .build()
        )
    ).when(mapper).map(
        Map.of(
            STARTING_URL, Set.of(page1Url, page2Url),
            page1Url, Set.of(doc2page1Url, doc2page2Url, STARTING_URL),
            page2Url, Set.of(),
            doc2page1Url, Set.of(),
            doc2page2Url, Set.of()
        )
    );

    // when
    CrawlResult result = underTest.crawl(STARTING_URL);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.getStartingUrl()).isEqualTo(STARTING_URL);
      softly.assertThat(result.getVisitedPages())
          .hasSize(5)
          .extracting("pageUrl", "hyperlinks")
          .containsExactlyInAnyOrder(
              tuple(STARTING_URL, Set.of(page1Url, page2Url)),
              tuple(page1Url, Set.of(doc2page1Url, doc2page2Url, STARTING_URL)),
              tuple(page2Url, Set.of()),
              tuple(doc2page1Url, Set.of()),
              tuple(doc2page2Url, Set.of())
          );
    });
  }
}