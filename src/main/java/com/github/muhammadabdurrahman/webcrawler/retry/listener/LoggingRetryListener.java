package com.github.muhammadabdurrahman.webcrawler.retry.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingRetryListener implements RetryListener {

  @Override
  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    log.warn("Retry attempt {}/{} for method {} failed due to: {}",
        context.getRetryCount(), context.getAttribute("context.max-attempts"), context.getAttribute("context.name"), throwable.getMessage());
  }
}
