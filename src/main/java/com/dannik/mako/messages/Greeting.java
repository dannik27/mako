package com.dannik.mako.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Greeting {

  private String message;
  private GameDto activeGame;
  private List<BotDto> bots;

  @Data
  @RequiredArgsConstructor
  public static class BotDto {
    private final String name;
  }
}
