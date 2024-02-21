package com.example.music.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
// Jackson 라이브러리가 camel case를 snake case로 바꿔준다.
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// Spotify에서 받은 토큰 정보
public class AccessTokenDto {
  private String accessToken;
  private String tokenType;
  private Integer expiresIn;
}
