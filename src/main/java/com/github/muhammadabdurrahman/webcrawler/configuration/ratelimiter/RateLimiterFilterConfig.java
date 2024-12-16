package com.github.muhammadabdurrahman.webcrawler.configuration.ratelimiter;

import com.github.muhammadabdurrahman.webcrawler.presentation.ratelimiter.RateLimiterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterFilterConfig {

  private final RateLimiterFilter rateLimiterFilter;

  @Bean
  public FilterRegistrationBean<RateLimiterFilter> rateLimiterFilterRegistrationBean() {
    FilterRegistrationBean<RateLimiterFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(rateLimiterFilter);
    registrationBean.addUrlPatterns("/api/*");
    return registrationBean;
  }
}