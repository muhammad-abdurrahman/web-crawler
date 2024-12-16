package com.github.muhammadabdurrahman.webcrawler.configuration.retry;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {

}
