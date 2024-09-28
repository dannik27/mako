package com.dannik.mako.messages;

import com.dannik.mako.model.GameState;
import lombok.Data;

import java.util.Map;

@Data
public class GameStateResponse {

  private final Map<String, GameState.PlayerState> players;

  public static GameStateResponse of(GameState state) {
    return new GameStateResponse(state.getPlayers());
  }

}
