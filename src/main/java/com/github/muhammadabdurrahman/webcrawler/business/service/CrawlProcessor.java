package com.github.muhammadabdurrahman.webcrawler.business.service;

import com.github.muhammadabdurrahman.webcrawler.business.client.WebHookClient;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.store.CrawlResultStore;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlProcessor {

  private final CrawlResultStore cache;
  private final WebHookClient<CrawlResult> webHookClient;
  private final WebCrawlerService webCrawlerService;

  @Async
  public void initiateCrawlingProcess(String startingUrl, String callbackUrl, UUID correlationId) {
    cache.get(startingUrl).ifPresentOrElse(
        crawlResult -> {
          webHookClient.send(callbackUrl, crawlResult, correlationId);
          cache.refreshTtl(startingUrl);
        },
        () -> {
          CrawlResult crawlResult = webCrawlerService.crawl(startingUrl);
          try {
            cache.put(crawlResult);
          } finally {
            webHookClient.send(callbackUrl, crawlResult, correlationId);
          }
        }
    );
  }
}
