package com.github.muhammadabdurrahman.webcrawler.persistence.model.mapper;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.persistence.model.CrawlResultEntity;
import org.mapstruct.Mapper;

@Mapper
public interface CrawlResultEntityMapper {

  CrawlResult map(CrawlResultEntity crawlResultEntity);

  CrawlResultEntity map(CrawlResult cralResult);
}
