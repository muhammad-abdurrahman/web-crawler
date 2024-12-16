package com.github.muhammadabdurrahman.webcrawler.configuration.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache")
@Getter
@Setter
public class CacheProperties {

  private Duration ttl;
  private Duration retentionDuration;
}
