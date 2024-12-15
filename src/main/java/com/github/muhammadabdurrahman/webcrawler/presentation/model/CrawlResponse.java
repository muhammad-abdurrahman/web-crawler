package com.github.muhammadabdurrahman.webcrawler.presentation.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CrawlResponse(
    @NotBlank(message = "The starting URL is required and cannot be blank")
    String startingUrl,
    @NotBlank(message = "The callback URL is required and cannot be blank")
    String callbackUrl
) {

}