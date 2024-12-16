package com.github.muhammadabdurrahman.webcrawler.presentation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muhammadabdurrahman.webcrawler.business.client.WebHookClient;
import com.github.muhammadabdurrahman.webcrawler.business.model.CrawlResult;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlAsyncResponse;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.mapper.CrawlAsyncResponseMapper;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlResultFileUploadClient implements WebHookClient<CrawlResult> {

  private final CrawlAsyncResponseMapper mapper;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void send(String url, CrawlResult crawlResult, UUID correlationId) {
    CrawlAsyncResponse payload = mapper.map(crawlResult);
    byte[] jsonData = convertToJsonBytes(payload);
    if (jsonData != null) {
      uploadFile(url, jsonData, correlationId);
    }
  }

  private byte[] convertToJsonBytes(CrawlAsyncResponse payload) {
    try {
      return objectMapper.writeValueAsBytes(payload);
    } catch (IOException e) {
      log.error("Error converting payload to JSON bytes", e);
      return null;
    }
  }

  private void uploadFile(String url, byte[] jsonData, UUID correlationId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ByteArrayResource(jsonData) {
      @Override
      public String getFilename() {
        return "crawl_response_%s.json".formatted(correlationId);
      }
    });

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    restTemplate.postForEntity(url, requestEntity, Void.class);
  }
}