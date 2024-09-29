package com.dannik.mako.messages;

import com.dannik.mako.model.GameState;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GameStateResponse {

  private final String activePlayer;
  private final List<PlayerStateDto> players;
  private final String phase;
  private final int lastDice;
  private final String lastBuilding;

  public static GameStateResponse of(GameState state) {
    return new GameStateResponse(state.getPlayers().get(state.getActivePlayer()).getUser().getUsername(),
        state.getPlayers().stream().map(PlayerStateDto::of).toList(),
        state.getPhase().name(),
        state.getLastDice(),
        state.getLastBuilding());
  }

  @Data
  @RequiredArgsConstructor
  public static class PlayerStateDto {
    private final String name;
    private final boolean online;
    private final int money;
    private final Map<String, Integer> cards;
    private final List<GameState.MoneyChange> lastMoneyChange;

    public static PlayerStateDto of(GameState.PlayerState state) {
      return new PlayerStateDto(state.getUser().getUsername(), state.getUser().isOnline(), state.getMoney(),
          state.getCards(), state.getLastMoneyChange());
    }
  }

}
