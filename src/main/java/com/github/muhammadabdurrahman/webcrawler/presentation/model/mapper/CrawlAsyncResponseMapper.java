package com.github.muhammadabdurrahman.webcrawler.presentation.model.mapper;

import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlAsyncResponse;
import org.mapstruct.Mapper;

@Mapper
public interface CrawlAsyncResponseMapper {

  CrawlAsyncResponse map(CrawlResult from);
}
