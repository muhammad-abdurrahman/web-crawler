package com.github.muhammadabdurrahman.webcrawler.business.store;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import java.util.Optional;

public interface CrawlResultStore {

  Optional<CrawlResult> get(String startingUrl);

  void refreshTtl(String startingUrl);

  void put(CrawlResult crawlResult);

  void evictStaleCrawlResults();
}
