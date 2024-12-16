package com.github.muhammadabdurrahman.webcrawler.integration;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muhammadabdurrahman.webcrawler.business.service.CrawlProcessor;
import com.github.muhammadabdurrahman.webcrawler.configuration.ratelimiter.RateLimitProperties;
import com.github.muhammadabdurrahman.webcrawler.presentation.advice.GlobalExceptionHandler;
import com.github.muhammadabdurrahman.webcrawler.presentation.controller.CrawlerController;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlRequest;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CrawlerController.class)
@Import({GlobalExceptionHandler.class, RateLimitProperties.class})
class ApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CrawlProcessor crawlProcessor;

  @MockitoBean
  private Supplier<UUID> uuidSupplier;

  @Test
  void should_accept_valid_request() throws Exception {
    CrawlRequest request = CrawlRequest.builder()
        .startingUrl("http://example.com")
        .callbackUrl("http://callback.com")
        .build();

    var expectedCorrelationId = UUID.randomUUID();
    doReturn(expectedCorrelationId).when(uuidSupplier).get();

    mockMvc.perform(post("/api/v1/crawler/crawl")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isAccepted())
        .andExpect(
            jsonPath("$.correlationId").value(expectedCorrelationId.toString())
        );
  }

  @Test
  void should_reject_bad_request_with_validation_errors() throws Exception {
    CrawlRequest invalidRequest = CrawlRequest.builder()
        .startingUrl("")
        .callbackUrl("")
        .build();

    mockMvc.perform(post("/api/v1/crawler/crawl")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation errors occurred"))
        .andExpect(jsonPath("$.detail").value(containsString("[field: startingUrl, error: The starting URL is required and cannot be blank]")))
        .andExpect(jsonPath("$.detail").value(containsString("[field: callbackUrl, error: The callback URL is required and cannot be blank]")))
        .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"));
  }

  @Test
  void should_reject_when_service_unavailable() throws Exception {
    doThrow(new RejectedExecutionException("Server busy")).when(crawlProcessor)
        .initiateCrawlingProcess(anyString(), anyString(), any(UUID.class));

    var expectedCorrelationId = UUID.randomUUID();
    doReturn(expectedCorrelationId).when(uuidSupplier).get();

    CrawlRequest request = CrawlRequest.builder()
        .startingUrl("http://example.com")
        .callbackUrl("http://callback.com")
        .build();

    mockMvc.perform(post("/api/v1/crawler/crawl")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.title").value("Service Unavailable"))
        .andExpect(jsonPath("$.status").value("SERVICE_UNAVAILABLE"))
        .andExpect(jsonPath("$.detail").value("The server is currently busy. Please try again later."))
        .andExpect(jsonPath("$.message").value("Server is busy"));
  }

  @Test
  void should_reject_when_internal_server_error() throws Exception {
    doThrow(new RuntimeException("What a disaster!")).when(crawlProcessor)
        .initiateCrawlingProcess(anyString(), anyString(),any(UUID.class));

    var expectedCorrelationId = UUID.randomUUID();
    doReturn(expectedCorrelationId).when(uuidSupplier).get();

    CrawlRequest request = CrawlRequest.builder()
        .startingUrl("http://example.com")
        .callbackUrl("http://callback.com")
        .build();

    mockMvc.perform(post("/api/v1/crawler/crawl")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.detail").value("An unexpected error occurred: What a disaster!"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
