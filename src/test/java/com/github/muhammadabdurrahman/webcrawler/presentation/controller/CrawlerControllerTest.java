package com.github.muhammadabdurrahman.webcrawler.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.webcrawler.business.service.CrawlProcessor;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlRequest;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlSyncResponse;
import com.github.muhammadabdurrahman.webcrawler.util.UuidSupplier;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlerControllerTest {

  @Mock
  private CrawlProcessor service;

  @Mock
  private UuidSupplier uuidSupplier;

  @InjectMocks
  private CrawlerController underTest;

  @Test
  void shouldCrawl() {
    // given
    var crawlRequest = CrawlRequest.builder()
        .startingUrl("startingUrl")
        .callbackUrl("callbackUrl")
        .build();
    var expectedUuid = mock(UUID.class);
    doReturn(expectedUuid).when(uuidSupplier).get();
    doNothing().when(service).initiateCrawlingProcess(eq(crawlRequest.startingUrl()), eq(crawlRequest.callbackUrl()), any(UUID.class));

    // when
    CrawlSyncResponse actual = underTest.crawl(crawlRequest);

    // then
    assertThat(actual).isNotNull();
    assertThat(actual.correlationId()).isEqualTo(expectedUuid);
    verify(service).initiateCrawlingProcess(eq(crawlRequest.startingUrl()), eq(crawlRequest.callbackUrl()), any(UUID.class));
  }

}