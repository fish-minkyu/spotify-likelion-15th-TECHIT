package com.example.music;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
  // 변수 이름을 일관성 있게 지은다면 똑같은 타입의 Bean 객체가 만들어져도
  // 정확하게 Bean 객체를 가져올 수 있다.
  private final RestClient spotifyClient;

  public Object search(
    String q,
    String type,
    String market,
    Integer limit,
    Integer offset
  ) {
    // /search?q=q&type=type&...
  String url = UriComponentsBuilder.fromUriString("/search")
    .queryParam("q", q)
    .queryParam("type", type)
    .queryParam("market", market)
    .queryParam("limit", limit)
    .queryParam("offset", offset)
    .build(false)
    .toUriString();

  // 문제점 2.
  // Bearer Token 준비
/*  String tokenHeaderValue
//    = "Bearer " + tokenService.getAccessToken().getAccessToken();
    = "Bearer " + tokenService.getToken();*/

    return spotifyClient.get()
      .uri(url)
      // Bearer Token 넣기
//      .header("Authorization", tokenHeaderValue)
      .retrieve()
      .body(Object.class);
  }
}
