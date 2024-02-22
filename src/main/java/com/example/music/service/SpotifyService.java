package com.example.music.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Map;

@Slf4j
@Service
public class SpotifyService {
  private final SpotifyHttpInterface spotifyClient;

  // HttpProxy를 만들어줘야 한다.
  // Client(: SpotifyClient) - Proxy(: HttpServiceProxyFactory을 통해 생성된 SpotifyClient)
  // Server(: Spotify)
  public SpotifyService(
    RestClient restClient
  ) {
    // Spotify와 통신을 담당할 SpotifyClient 생성
    this.spotifyClient = HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(restClient))
      .build()
      .createClient(SpotifyHttpInterface.class);
  }

  public Object search(Map<String, ?> params) {
    return spotifyClient.search(params);
  }

  public Object getAlbum(String id) {
    return spotifyClient.getAlbums(id);
  }

  public Object getArtist(String id) {
    return spotifyClient.getArtist(id);
  }

  public Object getTrack(String id) {
    return spotifyClient.getTrack(id);
  }
}
