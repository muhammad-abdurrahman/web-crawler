package com.github.muhammadabdurrahman.webcrawler.business.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.webcrawler.business.client.WebHookClient;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.store.CrawlResultStore;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlProcessorTest {

  @Mock
  private CrawlResultStore cache;

  @Mock
  private WebHookClient<CrawlResult> webHookClient;

  @Mock
  private WebCrawlerService webCrawlerService;

  @InjectMocks
  private CrawlProcessor underTest;

  @Mock
  private UUID correlationId;

  @Test
  void shouldSendCachedCrawlResultWhenAvailable() {
    // given
    var startingUrl = "http://example.com";
    var callbackUrl = "http://example.com/callback";
    var crawlResult = mock(CrawlResult.class);
    doReturn(Optional.of(crawlResult)).when(cache).get(startingUrl);

    // when
    underTest.initiateCrawlingProcess(startingUrl, callbackUrl, correlationId);

    // then
    verify(webHookClient).send(callbackUrl, crawlResult, correlationId);
    verify(cache).refreshTtl(startingUrl);
  }

  @Test
  void shouldCrawlAndSendResultWhenNoCachedCrawlResult() {
    // given
    var startingUrl = "http://example.com";
    var callbackUrl = "http://example.com/callback";
    var crawlResult = mock(CrawlResult.class);
    doReturn(Optional.empty()).when(cache).get(startingUrl);
    doReturn(crawlResult).when(webCrawlerService).crawl(startingUrl);

    // when
    underTest.initiateCrawlingProcess(startingUrl, callbackUrl, correlationId);

    // then
    verify(cache).put(crawlResult);
    verify(webHookClient).send(callbackUrl, crawlResult, correlationId);
  }

}