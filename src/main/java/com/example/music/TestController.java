package com.example.music;

import com.example.music.dto.AccessTokenDto;
import com.example.music.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
// 테스트 해보기 위한 용도
public class TestController {
  private final SpotifyTokenService tokenService;
  private final SpotifyService spotifyService;

  // 검색
  @GetMapping("/search")
  public Object search(@RequestParam Map<String, ?> params) {
    return spotifyService.search(params);
  }

  // spotifyId와 type으로 해당 객체 검색
  @GetMapping("/get-entity")
  public Object getEntity(
    @RequestParam("spotifyId") String id,
    @RequestParam("type") String type
  ) {
//    switch (type) {
//      case "album":
//        return spotifyService.getAlbum(id);
//      case "artist":
//        return spotifyService.getArtist(id);
//      case "track":
//        return spotifyService.getTrack(id);
//      default:
//        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//    }

    return switch (type) {
      case "album" -> spotifyService.getAlbum(id);
      case "artist" -> spotifyService.getArtist(id);
      case "track" -> spotifyService.getTrack(id);
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    };
  }

/*
// accessToken 얻기 테스트
@GetMapping("/test/token")
  public AccessTokenDto getAccessToken() {
    return tokenService.getAccessToken();
  }
  */
}
