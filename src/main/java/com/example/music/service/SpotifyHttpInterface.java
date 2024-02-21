package com.example.music.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

// Spotify에서 사용할 API들을 정리한다.
public interface SpotifyHttpInterface {
  @GetExchange("/search")
  Object search(@RequestParam Map<String, ?> params);

  @GetExchange("/albums/{id}")
  Object getAlbums(@PathVariable("id") String id);

  @GetExchange("/artists/{id}")
  Object getArtist(@PathVariable("id") String id);

  @GetExchange("/tracks/{id}")
  Object getTrack(@PathVariable("id") String id);
}
