package com.dannik.mako.messages;

import com.dannik.mako.model.Game;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GamesUpdate {

  private final Map<String, GameDto> games;

  public static GamesUpdate of(Game game) {
    return new GamesUpdate(Map.of(game.getId(), GameDto.of(game)));
  }

  public static GamesUpdate deletedOf(Game game) {
    Map<String, GameDto> updates = new HashMap<>();
    updates.put(game.getId(), null);
    return new GamesUpdate(updates);
  }

}
