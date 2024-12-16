package com.github.muhammadabdurrahman.webcrawler.business.client;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RobotClient {

  private static final String ROBOTS_TXT_PATH = "/robots.txt";

  private final RestTemplate restTemplate;

  public Set<String> getDisallowedPaths(URI baseUrl) {
    URI robotsUrl = URI.create("%s://%s:%d%s".formatted(baseUrl.getScheme(), baseUrl.getHost(), baseUrl.getPort(), ROBOTS_TXT_PATH));
    String robotsContent = restTemplate.getForObject(robotsUrl, String.class);

    if (robotsContent == null || robotsContent.isBlank()) {
      return Set.of();
    }

    // Parse and group lines by User-agent sections
    var groupedSections = Arrays.stream(robotsContent.split("\n"))
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .collect(Collectors.groupingBy(
            new GroupingByUserAgentFunction(),
            LinkedHashMap::new,
            toList()
        ));

    // Filter for User-agent: * and extract disallowed paths
    return groupedSections.values().stream()
        .filter(this::isWildcardUserAgentSection)
        .flatMap(section -> section.stream()
            .filter(line -> line.startsWith("Disallow:"))
            .map(line -> line.substring("Disallow:".length()).trim()))
        .collect(toSet());
  }

  private boolean isWildcardUserAgentSection(List<String> section) {
    return section.stream().anyMatch(line -> line.equalsIgnoreCase("User-agent: *"));
  }

  private static class GroupingByUserAgentFunction implements Function<String, Integer> {

    private int group = 0;

    @Override
    public Integer apply(String line) {
      if (line.startsWith("User-agent:")) {
        group++;
      }
      return group;
    }
  }
}

