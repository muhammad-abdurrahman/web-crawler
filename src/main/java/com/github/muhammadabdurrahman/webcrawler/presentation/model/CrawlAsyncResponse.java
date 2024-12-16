package com.github.muhammadabdurrahman.webcrawler.presentation.model;

import java.util.Set;
import lombok.Builder;

@Builder
public record CrawlAsyncResponse(
    String startingUrl,
    Set<Page> visitedPages
) {

  public record Page(
      String pageUrl,
      Set<String> hyperlinks
  ) {

  }

}