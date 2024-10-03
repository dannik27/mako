package com.dannik.mako.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GameState {

  private Game game;
  private List<PlayerState> players;

  private int activePlayer;
  private GamePhase phase;
  private int lastDice;
  private String lastBuilding;

  public static GameState init(Game game) {
    GameState state = new GameState();
    state.game = game;
    state.players = game.getPlayers().stream().map(PlayerState::new).toList();
    state.phase = GamePhase.DICE;
    return state;
  }

  public enum GamePhase {
    DICE,
    CHOICE,
    BUILD
  }

  @Data
  public static class PlayerState {

    private User user;
    private int money;
    private Map<String, Integer> cards = new HashMap<>();
    private List<MoneyChange> lastMoneyChange = new ArrayList<>();

    public PlayerState(User user) {
      this.user = user;
      this.money = 3;
      cards.put("Bakery", 1);
      cards.put("Wheat", 1);
    }

  }

  @Data
  @RequiredArgsConstructor
  public static class MoneyChange {
    private final String card;
    private final int count;
    private final String opponent;
  }

//  @Data
//  public static class CardState {
//    private String name;
//    private int count;
//    private int lastMoneyChange;
//  }

}
