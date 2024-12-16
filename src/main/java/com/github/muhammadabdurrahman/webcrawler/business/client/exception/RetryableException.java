package com.github.muhammadabdurrahman.webcrawler.business.client.exception;

import org.springframework.http.HttpStatus;

public class RetryableException extends RuntimeException {

  public RetryableException(String url, HttpStatus status) {
    super("Request to %s failed with status: %s".formatted( url, status));
  }
}
