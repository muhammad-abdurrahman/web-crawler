package com.github.muhammadabdurrahman.webcrawler.presentation.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlAsyncResponse;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.mapper.CrawlAsyncResponseMapper;
import java.time.Clock;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class CrawlResultFileUploadClientTest {

  @Mock
  private CrawlAsyncResponseMapper mapper;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private Clock clock;

  @InjectMocks
  private CrawlResultFileUploadClient underTest;

  @Mock
  private UUID correlationId;


  @Test
  @SneakyThrows
  void shouldSend() {
    // given
    var url = "pageUrl";
    var crawlResult = mock(CrawlResult.class);
    var crawlResponse = mock(CrawlAsyncResponse.class);
    doReturn(crawlResponse).when(mapper).map(crawlResult);
    doReturn("bytes".getBytes()).when(objectMapper).writeValueAsBytes(crawlResponse);
    var responseEntity = mock(ResponseEntity.class);
    doReturn(responseEntity).when(restTemplate).postForEntity(eq(url), any(HttpEntity.class), eq(Void.class));
    // when
    underTest.send(url, crawlResult, correlationId);

    // then
    verify(restTemplate).postForEntity(eq(url), any(HttpEntity.class), eq(Void.class));
  }
}