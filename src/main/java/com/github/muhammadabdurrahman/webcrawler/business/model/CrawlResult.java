package com.github.muhammadabdurrahman.webcrawler.business.model;

import com.github.muhammadabdurrahman.webcrawler.concurrency.model.Lockable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class CrawlResult implements Lockable {

  private String startingUrl;
  private Set<Page> visitedPages = new HashSet<>();

  @Override
  public Object getLockKey() {
    return this.startingUrl;
  }

  @Data
  @Builder
  @EqualsAndHashCode(of = "pageUrl")
  public static final class Page {

    private String pageUrl;
    private Set<String> hyperlinks;
  }
}
