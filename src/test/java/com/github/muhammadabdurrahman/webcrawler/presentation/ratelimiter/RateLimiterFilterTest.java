package com.github.muhammadabdurrahman.webcrawler.presentation.ratelimiter;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.muhammadabdurrahman.webcrawler.configuration.ratelimiter.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.Duration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RateLimiterFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private RateLimiterFilter underTest;

  @BeforeEach
  void setUp() {
    underTest = new RateLimiterFilter(
        RateLimitProperties.builder()
            .tokens(1)
            .refillPeriod(Duration.ofMinutes(1))
            .capacity(1)
            .build()
    );

    // given
    String clientIp = "192.168.0.1";
    doReturn(clientIp).when(request).getRemoteAddr();
  }

  @Test
  @SneakyThrows
  void shouldAllowRequestWhenBucketHasTokens() {
    // when
    underTest.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @SneakyThrows
  void shouldRejectRequestWhenBucketHasNoTokens() {
    // given
    doNothing().when(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    var printWriter = mock(PrintWriter.class);
    doReturn(printWriter).when(response).getWriter();
    doNothing().when(printWriter).write("Too many requests");

    // when
    underTest.doFilterInternal(request, response, filterChain);
    underTest.doFilterInternal(request, response, filterChain);

    // then
    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    verify(printWriter).write("Too many requests");
  }
}