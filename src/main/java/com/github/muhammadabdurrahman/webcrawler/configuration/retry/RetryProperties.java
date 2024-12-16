package com.github.muhammadabdurrahman.webcrawler.configuration.retry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "retry")
public class RetryProperties {

  private int maxAttempts = 9;
  private BackoffProperties backoff = new BackoffProperties();

  @Getter
  @Setter
  public static class BackoffProperties {

    private long delay = 5000;
    private double multiplier = 2;
  }
}
