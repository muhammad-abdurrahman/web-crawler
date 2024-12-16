package com.github.muhammadabdurrahman.webcrawler.business.client;

import java.util.UUID;

public interface WebHookClient<T> {

  void send(String url, T payload, UUID correlationId);
}
