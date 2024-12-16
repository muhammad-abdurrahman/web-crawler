package com.github.muhammadabdurrahman.webcrawler.util;

import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class UuidSupplier implements Supplier<UUID> {

  @Override
  public UUID get() {
    return UUID.randomUUID();
  }
}
