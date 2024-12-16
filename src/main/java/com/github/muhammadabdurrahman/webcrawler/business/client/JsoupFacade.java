package com.github.muhammadabdurrahman.webcrawler.business.client;

import com.github.muhammadabdurrahman.webcrawler.business.client.exception.RetryableException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsoupFacade {

  @Retryable(
      retryFor = {RetryableException.class},
      maxAttemptsExpression = "#{@retryProperties.maxAttempts}",
      backoff = @Backoff(
          delayExpression = "#{@retryProperties.backoff.delay}",
          multiplierExpression = "#{@retryProperties.backoff.multiplier}"
      ),
      listeners = {"loggingRetryListener"}
  )
  public Stream<String> getHyperlinks(String pageUrl) {
    try {
      log.debug("Fetching page: {}", pageUrl);
      return Jsoup.connect(pageUrl).get()
          .stream()
          .map(document -> document.select("a[href]"))
          .flatMap(Elements::stream)
          .map(link -> link.absUrl("href"));
    } catch (UnsupportedMimeTypeException ume) {
      // skip unsupported mime types
      return Stream.empty();
    } catch (HttpStatusException e) {
      if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value() || e.getStatusCode() >= 500) {
        // trigger retry
        throw new RetryableException(e.getUrl(), HttpStatus.valueOf(e.getStatusCode()));
      } else if (e.getStatusCode() != HttpStatus.NOT_FOUND.value()) {
        log.warn("Failed to fetch content from: {}", pageUrl, e);
      }
    } catch (Exception e) {
      log.warn("Failed to fetch content from: {}", pageUrl, e);
    }
    return Stream.empty();
  }
}
