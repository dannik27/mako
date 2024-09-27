package com.dannik.mako.messages;

import com.dannik.mako.model.Game;
import lombok.Data;

import java.util.Map;

@Data
public class GamesUpdate {

  private final Map<String, ListGamesResponse.GameDto> games;

  public static GamesUpdate of(Game game) {
    return new GamesUpdate(Map.of(game.getId(), ListGamesResponse.GameDto.of(game)));
  }

}
