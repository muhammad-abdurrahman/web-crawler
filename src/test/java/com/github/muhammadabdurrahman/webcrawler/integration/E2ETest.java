package com.github.muhammadabdurrahman.webcrawler.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muhammadabdurrahman.webcrawler.WebCrawlerApplication;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlAsyncResponse;
import com.github.muhammadabdurrahman.webcrawler.presentation.model.CrawlRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(classes = WebCrawlerApplication.class)
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8089)
class E2ETest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void crawl() throws Exception {
    // given
    stubFor(get(urlEqualTo("/starting-page.html"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html")
            .withBody("""
                <html>
                  <body>
                    <a href="/linked-page1.html">Link 1</a>
                    <a href="/linked-page2.html">Link 2</a>
                  </body>
                </html>
                """)
        )
    );
    stubFor(get(urlEqualTo("/linked-page1.html"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html")
            .withBody("""
                <html>
                  <body>
                    <p>Linked Page 1</p>
                    <a href="/linked-page1-1.html">Link 1</a>
                    <a href="/linked-page1-2.html">Link 2</a>
                    <a href="/starting-page.html">Starting Page</a>
                  </body>
                </html>
                """)
        )
    );

    stubFor(get(urlEqualTo("/linked-page1-1.html"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html")
            .withBody("""
                <html>
                  <body>
                    <p>Linked Page 1.1</p>
                    <a href="/starting-page.html">Starting Page</a>
                  </body>
                </html>
                """)
        )
    );

    stubFor(get(urlEqualTo("/linked-page1-2.html"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html")
            .withBody("""
                <html>
                  <body>
                    <p>Linked Page 1.2</p>
                    <a href="/starting-page.html">Starting Page</a>
                  </body>
                </html>
                """)
        )
    );

    stubFor(get(urlEqualTo("/linked-page2.html"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html")
            .withBody("""
                <html>
                  <body>
                    <p>Linked Page 2</p>
                    <a href="/starting-page.html">Starting Page</a>
                    <a href="/disallowed/secret-page.html">Link 1.2</a>
                  </body>
                </html>
                """)
        )
    );

    // robots.txt
    stubFor(get(urlEqualTo("/robots.txt"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody("User-agent: *\nDisallow: /disallowed")));

    // callback endpoint
    stubFor(post(urlEqualTo("/callback"))
        .willReturn(aResponse()
            .withStatus(200)));

    CrawlRequest request = CrawlRequest.builder()
        .startingUrl("http://localhost:8089/starting-page.html")
        .callbackUrl("http://localhost:8089/callback")
        .build();

    // when
    String correlationId = JsonPath.parse(mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/crawler/crawl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.correlationId").isNotEmpty())
            .andReturn()
            .getResponse().getContentAsString()
        ).read("$.correlationId");

    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(postRequestedFor(urlEqualTo("/callback")))
        );

    // then
    List<LoggedRequest> callbackRequests = findAll(postRequestedFor(urlEqualTo("/callback")));
    assertThat(callbackRequests).hasSize(1);

    LoggedRequest callbackRequest = callbackRequests.getFirst();

    String contentType = callbackRequest.getHeader("Content-Type");
    assertThat(contentType).contains("multipart/form-data");

    String filename = extractFilenameFromMultipart(callbackRequest);
    assertThat(filename).isEqualTo("crawl_response_%s.json".formatted(correlationId));

    String jsonPayload = extractFileFromMultipart(callbackRequest);

    CrawlAsyncResponse crawlAsyncResponse = objectMapper.readValue(jsonPayload, CrawlAsyncResponse.class);

    assertThat(crawlAsyncResponse.startingUrl()).isEqualTo("http://localhost:8089/starting-page.html");
    assertThat(crawlAsyncResponse.visitedPages()).hasSize(5);

    assertThat(crawlAsyncResponse.visitedPages())
        .extracting(CrawlAsyncResponse.Page::pageUrl, CrawlAsyncResponse.Page::hyperlinks)
        .containsExactlyInAnyOrder(
            tuple(
                "http://localhost:8089/starting-page.html",
                Set.of(
                    "http://localhost:8089/linked-page1.html",
                    "http://localhost:8089/linked-page2.html"
                )
            ),
            tuple(
                "http://localhost:8089/linked-page1.html",
                Set.of(
                    "http://localhost:8089/linked-page1-1.html",
                    "http://localhost:8089/linked-page1-2.html",
                    "http://localhost:8089/starting-page.html"
                )
            ),
            tuple(
                "http://localhost:8089/linked-page1-1.html",
                Set.of(
                    "http://localhost:8089/starting-page.html"
                )
            ),
            tuple(
                "http://localhost:8089/linked-page1-2.html",
                Set.of(
                    "http://localhost:8089/starting-page.html"
                )
            ),
            tuple(
                "http://localhost:8089/linked-page2.html",
                Set.of(
                    "http://localhost:8089/starting-page.html",
                    "http://localhost:8089/disallowed/secret-page.html"
                )
            )
        );
  }

  private String extractFilenameFromMultipart(LoggedRequest callbackRequest) {
    String body = callbackRequest.getBodyAsString();
    String boundary = "--" + callbackRequest.getHeader("Content-Type").split("boundary=")[1];

    String[] parts = body.split(boundary);
    for (String part : parts) {
      if (part.contains("Content-Disposition: form-data; name=\"file\"")) {
        int start = part.indexOf("filename=\"") + 10;
        int end = part.indexOf("\"", start);
        return part.substring(start, end);
      }
    }
    return null;
  }

  private String extractFileFromMultipart(LoggedRequest request) {
    String body = request.getBodyAsString();
    String boundary = "--" + request.getHeader("Content-Type").split("boundary=")[1];

    String[] parts = body.split(boundary);
    for (String part : parts) {
      if (part.contains("Content-Disposition: form-data; name=\"file\"")) {
        int start = part.indexOf("\r\n\r\n") + 4;
        int end = part.lastIndexOf("\r\n");
        return part.substring(start, end);
      }
    }
    return null;
  }
}
