package com.example.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpotifyRestConfig {
  @Bean
  public RestClient spotifyClient() {
    return RestClient.builder()
      .baseUrl("https://api.spotify.com/v1")
      .build();
  }
}
