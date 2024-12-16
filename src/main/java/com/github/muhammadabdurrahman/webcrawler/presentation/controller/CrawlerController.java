package com.github.muhammadabdurrahman.webcrawler.presentation.controller;

import com.github.muhammadabdurrahman.webcrawler.business.service.CrawlProcessor;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlRequest;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlSyncResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

  private final CrawlProcessor crawlProcessor;

  private final Supplier<UUID> uuidSupplier;

  @PostMapping(value = "/crawl", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public CrawlSyncResponse crawl(@RequestBody @Valid CrawlRequest crawlRequest) {
    UUID correlationId = uuidSupplier.get();
    crawlProcessor.initiateCrawlingProcess(crawlRequest.startingUrl(), crawlRequest.callbackUrl(), correlationId);
    return new CrawlSyncResponse(correlationId);
  }
}
