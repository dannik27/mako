package com.dannik.mako.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GameState {

  private Game game;
  private Map<String, PlayerState> players;



  public static GameState init(Game game) {
    GameState state = new GameState();
    state.game = game;
    state.players = game.getPlayers().stream().collect(Collectors.toMap(User::getUsername, (player) -> new PlayerState()));
    return state;
  }

  @Data
  public static class PlayerState {

    private int money = 3;
    private Map<String, Integer> cards = new HashMap<>();

    {
      cards.put("bakery", 1);
      cards.put("pshen", 1);
    }



  }

}
