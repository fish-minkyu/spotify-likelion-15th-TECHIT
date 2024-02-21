package com.example.music;

import com.example.music.dto.AccessTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
// 테스트 해보기 위한 용도
public class TestController {
  private final SpotifyTokenService tokenService;

  @GetMapping("/test/token")
  public AccessTokenDto getAccessToken() {
    return tokenService.getAccessToken();
  }
}
