package com.github.muhammadabdurrahman.webcrawler.business.model.mapper;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult.Page;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

@Mapper
public interface CrawlResultLinkMapper {

  default Set<Page> map(Map<String, Set<String>> from) {
    return from.entrySet().stream()
        .map(entry -> Page.builder()
            .pageUrl(entry.getKey())
            .hyperlinks(entry.getValue())
            .build())
        .collect(Collectors.toSet());
  }
}
