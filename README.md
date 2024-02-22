# Music - Spotify API 활용
- 2024.02.20 `15주차`

## 스팩

- Spring Boot 3.2.2
- Spring Web
- Spring Data JPA
- Lombok
- SQLite

## Key Point

- `RestClient`  
[SpotifyRestConfig](/src/main/java/com/example/music/config/SpotifyRestConfig.java)  
=> RestClient를 설정하고, tokenService의 getToken()에서 accessToken을 발급해온다.
```java
@Configuration
@RequiredArgsConstructor
public class SpotifyRestConfig {
  private final SpotifyTokenService tokenService;

  @Bean
  public RestClient spotifyClient() {
    return RestClient.builder()
      .baseUrl("https://api.spotify.com/v1")
      // 이 RestClient로 보내지는 모든 요청에 기본 헤더를 포함하는
      // 메서드를 전달한다.
      .requestInitializer(request -> request.getHeaders()
        // "Bearer " + token
        .setBearerAuth(tokenService.getToken()))
      .build();
  }
}
```

[SpotifyTokenService](/src/main/java/com/example/music/service/SpotifyService.java)  
=> accessToken을 생성하고 재발급하는 로직이 들어가 있는 클래스다.  
Bean으로 등록이 되어있고 RestClient 생성 시, 토큰이 자동으로 생성이 되게  
`SpotifyRestConfig`에 설정이 되어 있다.  

이 때, Spring Boot가 SpotifyTokenService을 생성하면  
생성자를 통해 reissue 메소드가 실행됨으로서 자동으로 accessToken이 생성된다.

- 생성자
```java
@Slf4j
@Component
// 요청을 보낼 때, clientId와 clientSecret을 포함 시켜서 보내게끔 해주는 용도다.
public class SpotifyTokenService {
  // Request Body가 변하지 않음으로 필드로 올리자
  private final MultiValueMap<String, Object> parts;

  // 마지막으로 accessToken을 발급한 시점
  private LocalDateTime lastIssued;

  // 현재 사용중인 Bearer Token
  private String token;

  private final RestClient authRestClient;

  // 생성자 DI
  public SpotifyTokenService(
    // application.yaml에 있는 설정값 가져와서 사용하기
    @Value("${spotify.client-id}") String clientId,
    @Value("${spotify.client-secret}") String clientSecret
  ) {
    // authRestConfig로 설정 클래스를 만들어두었으나
    // 해당 클래스에서만 사용을 해서 생성자로 가지고 왔다.
    this.authRestClient = RestClient.builder()
      .baseUrl("https://accounts.spotify.com/api/token")
      .build();
    ;

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
  // ...
}
```

- reissue Method  
=> accessToken 생성 및 재발행하는 메서드
```java
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
```

- getToken  
=> accessToken 유효시간 확인을 하며 50분이 지나면 reissue()를 호출하여 재발행
```java
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
```

[SearchService](/src/main/java/com/example/music/SearchService.java)  
=> RestClient를 활용한 검색 method
```java
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

    return spotifyClient.get()
      .uri(url)
      .retrieve()
      .body(Object.class);
  }
}
```

[SearchController](/src/main/java/com/example/music/SearchController.java)
```java
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
  private final SearchService service;

  @GetMapping
  public Object search(
    @RequestParam("q")
    String q,
    @RequestParam(value = "type", defaultValue = "album,artist,track")
    String type,
    @RequestParam(value = "market", defaultValue = "KR")
    String market,
    @RequestParam(value = "limit", defaultValue = "5")
    Integer limit,
    @RequestParam(value = "offset", defaultValue = "0")
    Integer offset
  ) {
    return service.search(q, type, market, limit, offset);
  }
}
```


- `Http Interface`

[SpotifyHttpInterface](/src/main/java/com/example/music/service/SpotifyHttpInterface.java)    
=> Spotify에서 사용할 API들을 정리하는 인터페이스
```java
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
```

[SpotifyService](/src/main/java/com/example/music/service/SpotifyService.java)  
- Http Proxy    
=> Client(= SpotifyClient) - Proxy(= HttpServiceProxyFactory을 통해 생성된 SpotifyClient) - Server(= Spotify)  
RestClient을 기반으로 생성된 spotifyClient는  
accessToken을 RestClient가 자동으로 관리해줌으로서 따로 발급을 할 필요가 없다.
```java
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

  // ...
}
```
- Method들
```java
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
```

[TestController](/src/main/java/com/example/music/TestController.java)  
=> Http Interface를 이용해 만든 Service를 사용하는 Controller  
(주석 처리된 getAccessToken 메소드는 RestClient의 accessToken 발급받아보는 테스트용이다.)
```java
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
  
    return switch (type) {
      case "album" -> spotifyService.getAlbum(id);
      case "artist" -> spotifyService.getArtist(id);
      case "track" -> spotifyService.getTrack(id);
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    };
  }
```

## 강사님 GitHub
DTO를 기반으로 프로젝트가 구성이 되어있다. 
코드를 참고하여 공부하는데 큰 도움이 될 것 같다.  
[likelion-backend-8-spotify](https://github.com/edujeeho0/likelion-backend-8-spotify)