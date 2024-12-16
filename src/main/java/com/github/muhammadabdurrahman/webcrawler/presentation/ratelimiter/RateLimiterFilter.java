package com.github.muhammadabdurrahman.webcrawler.presentation.ratelimiter;

import com.github.muhammadabdurrahman.webcrawler.configuration.ratelimiter.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Setter
public class RateLimiterFilter extends OncePerRequestFilter {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private final RateLimitProperties rateLimitProperties;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String clientIp = request.getRemoteAddr();
    Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("Too many requests");
    }
  }

  private Bucket newBucket(String clientIp) {
    Refill refill = Refill.greedy(rateLimitProperties.getTokens(), rateLimitProperties.getRefillPeriod());
    Bandwidth limit = Bandwidth.classic(rateLimitProperties.getCapacity(), refill);
    return Bucket.builder().addLimit(limit).build();
  }
}
