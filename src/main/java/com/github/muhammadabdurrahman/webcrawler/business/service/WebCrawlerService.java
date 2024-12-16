package com.github.muhammadabdurrahman.webcrawler.business.service;

import static com.google.common.net.UrlEscapers.urlFragmentEscaper;

import com.github.muhammadabdurrahman.webcrawler.business.client.JsoupFacade;
import com.github.muhammadabdurrahman.webcrawler.business.client.RobotClient;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.business.model.mapper.CrawlResultLinkMapper;
import com.google.common.net.InternetDomainName;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
class WebCrawlerService {

  private final RobotClient robotClient;
  private final JsoupFacade jsoupFacade;
  private final CrawlResultLinkMapper linkMapper;

  public CrawlResult crawl(String startingUrl) {
    URI startingUri = URI.create(urlFragmentEscaper().escape(startingUrl));
    Set<String> disallowedPaths = robotClient.getDisallowedPaths(startingUri);
    Map<String, Set<String>> visitedLinks = crawl(startingUri.toString(), startingUri.getHost(), disallowedPaths);
    return CrawlResult.builder()
        .startingUrl(startingUri.toString())
        .visitedPages(linkMapper.map(visitedLinks))
        .build();
  }

  private Map<String, Set<String>> crawl(String startUrl, String baseDomain, Set<String> disallowedPaths) {
    BlockingQueue<String> urlFrontier = new LinkedBlockingQueue<>();
    urlFrontier.offer(startUrl);

    var visitedPageHyperlinks = new ConcurrentHashMap<String, Set<String>>();

    try (ExecutorService executor = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() * 2)) {
      Semaphore semaphore = new Semaphore(0);

      while (!urlFrontier.isEmpty() || semaphore.availablePermits() > 0) {
        String url = urlFrontier.poll();
        if (url != null) {
          boolean taskSubmitted = submitFetchTask(
              url, baseDomain, disallowedPaths, urlFrontier, visitedPageHyperlinks, semaphore, executor
          );
          if (!taskSubmitted) {
            log.error("Failed to submit task for URL: {}", url);
          }
        }
      }

      shutdownExecutor(executor);
    }

    return visitedPageHyperlinks;
  }

  private boolean submitFetchTask(String url, String baseDomain, Set<String> disallowedPaths,
      BlockingQueue<String> urlQueue, Map<String, Set<String>> visitedPageHyperlinks,
      Semaphore semaphore, ExecutorService executor) {
    try {
      semaphore.release(); // increment the available permits, signalling task is in progress
      executor.submit(() -> {
        try {
          if (!visitedPageHyperlinks.containsKey(url) && isCrawlingAllowed(url, disallowedPaths)) {
            Set<String> hyperlinks = fetchHyperlinks(url, baseDomain, urlQueue, visitedPageHyperlinks);
            visitedPageHyperlinks.put(url, hyperlinks);
          }
        } catch (Exception e) {
          log.error("Error processing URL: {}", url, e);
        } finally {
          semaphore.acquireUninterruptibly();
        }
      });
      return true;
    } catch (Exception e) {
      log.error("Error submitting task for URL: {}", url, e);
      semaphore.acquireUninterruptibly(); // decrement the available permits, signalling task is complete
      return false;
    }
  }

  private Set<String> fetchHyperlinks(String url, String baseDomain, BlockingQueue<String> urlQueue,
      Map<String, Set<String>> visitedPageHyperlinks) {

    Set<String> hyperlinks = jsoupFacade.getHyperlinks(url)
        .filter(link -> isSameSubdomain(link, baseDomain))
        .collect(Collectors.toSet());

    hyperlinks.stream()
        .filter(link -> !visitedPageHyperlinks.containsKey(link))
        .forEach(urlQueue::offer);

    return hyperlinks;
  }

  private void shutdownExecutor(ExecutorService executor) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
        log.warn("Executor did not terminate within the timeout.");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Executor shutdown interrupted.", e);
      executor.shutdownNow();
    }
  }

  private boolean isCrawlingAllowed(String url, Set<String> disallowedPaths) {
    try {
      URI uri = URI.create(urlFragmentEscaper().escape(url));
      String path = uri.getPath();
      return disallowedPaths.stream().noneMatch(path::startsWith);
    } catch (Exception e) {
      log.warn("Failed to parse URL: {}", url, e);
      return true;
    }
  }

  private static boolean isSameSubdomain(String url, String baseDomain) {
    InternetDomainName internetDomainName = InternetDomainName.from(baseDomain);
    return !internetDomainName.isTopPrivateDomain() || internetDomainName
        .topPrivateDomain()
        .toString()
        .equals(URI.create(urlFragmentEscaper().escape(url)).getHost());
  }
}
