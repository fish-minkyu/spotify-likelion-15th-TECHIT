package com.example.music;

import com.example.music.dto.AccessTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
// 요청을 보낼 때, clientId와 clientSecret을 포함 시켜서 보내게끔 해주는 용도다.
public class SpotifyTokenService {
  // application.yaml에 있는 설정값 가져와서 사용하기
  @Value("${spotify.client-id}")
  private String clientId;

  @Value("${spotify.client-secret}")
  private String clientSecret;

  private final RestClient authRestClient;

  // 생성자 DI
  public SpotifyTokenService(RestClient authRestClient) {
    this.authRestClient = authRestClient;
  }

  public AccessTokenDto getAccessToken() {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("grant_type", "client_credentials");
    parts.add("client_id", clientId);
    parts.add("client_secret", clientSecret);

    return authRestClient.post()
      // Content-Type 헤더 설정
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(parts)
      .retrieve()
      .body(AccessTokenDto.class);
  }
}
