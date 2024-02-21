package com.example.music;

import com.example.music.dto.AccessTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
  private final SpotifyTokenService tokenService;

  @GetMapping("/test/token")
  public AccessTokenDto getAccessToken() {
    return tokenService.getAccessToken();
  }
}
