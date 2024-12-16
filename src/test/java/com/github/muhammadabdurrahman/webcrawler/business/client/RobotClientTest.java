package com.github.muhammadabdurrahman.webcrawler.business.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RobotClientTest {

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private RobotClient underTest;

  @Test
  void shouldGetDisallowedPaths() {
    // given
    var url = "https://www.example.com";
    var robotsContent = """
        User-agent: *
        Disallow: /private/
        Disallow: /tmp/
        Disallow: /admin/
        
        User-agent: Googlebot
        Disallow: /no-google/
        
        User-agent: Bingbot
        Disallow: /no-bing/
        """;
    doReturn(robotsContent).when(restTemplate).getForObject(URI.create("%s:-1/robots.txt".formatted(url)), String.class);

    // when
    Set<String> actual = underTest.getDisallowedPaths(URI.create(url));

    // then
    assertThat(actual)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            "/private/",
            "/tmp/",
            "/admin/"
        );
  }

  @Test
  void shouldGetEmptySetWhenEmptyRobots() {
    // given
    var url = "https://www.example.com";
    doReturn("").when(restTemplate).getForObject(URI.create("%s:-1/robots.txt".formatted(url)), String.class);

    // when
    Set<String> actual = underTest.getDisallowedPaths(URI.create(url));

    // then
    assertThat(actual).isEmpty();
  }

  @Test
  void shouldGetEmptySetWhenNoRobots() {
    // given
    var url = "https://www.example.com";
    doReturn(null).when(restTemplate).getForObject(URI.create("%s:-1/robots.txt".formatted(url)), String.class);

    // when
    Set<String> actual = underTest.getDisallowedPaths(URI.create(url));

    // then
    assertThat(actual).isEmpty();
  }
}