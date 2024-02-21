package com.example.music;

import com.example.music.dto.AccessTokenDto;
import jakarta.persistence.Access;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Component
// 요청을 보낼 때, clientId와 clientSecret을 포함 시켜서 보내게끔 해주는 용도다.
public class SpotifyTokenService {
  // Request Body가 변하지 않음으로 필드로 올리자
  private final MultiValueMap<String, Object> parts; // 31분쯤 다시 봐보자

  // 마지막으로 accessToken을 발급한 시점
  private LocalDateTime lastIssued;

  // 현재 사용중인 Bearer Token
  private String token;

  private final RestClient authRestClient;

  // 생성자 DI
  public SpotifyTokenService(
    RestClient authRestClient,
    // application.yaml에 있는 설정값 가져와서 사용하기
    @Value("${spotify.client-id}") String clientId,
    @Value("${spotify.client-secret}") String clientSecret
  ) {
    this.authRestClient = authRestClient;

    // private final MultiValueMap<String, Object> parts;을 초기화 하는 방법
    // 항상 같은 Request Body를 보내게 됨으로,
    // 해당 Request Body는 저장을 해두자.
    this.parts = new LinkedMultiValueMap<>();
    this.parts.add("grant_type", "client_credentials");
    this.parts.add("client_id", clientId);
    this.parts.add("client_secret", clientSecret);

    // 처음으로 SpotifyTokenService가 만들어질 때,
    // reissue()가 실행이 되면서
    // Token과 lastIssued가 기록이 된다.
    reissue();
  }

  // Token을 재발행하는 메서드
  private void reissue() {
    log.info("issuing access token");
    // 1. Token을 발행받는다.
    AccessTokenDto response = authRestClient.post()
      // Content-Type 헤더 설정
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(parts)
      .retrieve()
      .body(AccessTokenDto.class);

    // 2. 내가 Token을 받은 시간을 기록한다.
    lastIssued = LocalDateTime.now();

    // 3. 새로 발급받은 Token을 기록한다.
    token = response.getAccessToken();
    log.info("new access token is: {}", token);
  }

  // 현재 사용중인 Token을 반환하는 메서드
  public String getToken() {
    log.info("last issued: {}", lastIssued);
    // Token 발생 시간에서 현재 시간까지 얼만큼 시간이 소요되었는지 확인
    log.info("time passed: {} mins", ChronoUnit.MINUTES.between(lastIssued, LocalDateTime.now()));
    // 만약 마지막에 발급받은지 50분이 지났다.
    if (lastIssued.isBefore(LocalDateTime.now().minusMinutes(50)))
      // 그럴 경우 재발행
      reissue();

    // 재발행을 했든 안했든 Token을 돌려준다.
    return token;
  }

/*
  @Deprecated
  public AccessTokenDto getAccessToken() {
    // 문제
    // 메서드가 실행될 때마다 AccessToken이 재발행된다.
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
*/
}
