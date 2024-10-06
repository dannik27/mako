package com.dannik.mako.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
public class GameState {

  private Game game;
  private List<PlayerState> players;

  private int activePlayer;
  private GamePhase phase;
  private int lastDice;
  private String lastBuilding;
  private int turn = 1;
  private boolean wasDouble = false;

  private Map<String, Map<String, Object>> confirmations = new HashMap<>();
  private String requiredConfirmation;

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
    BUILD,
    WINNER
  }

  @Data
  public static class PlayerState {

    private User user;
    private int money;
    private Map<String, Integer> cards = new HashMap<>();
    private List<MoneyChange> lastMoneyChange = new ArrayList<>();
    private String lastBoughtCard;

    public PlayerState(User user) {
      this.user = user;
      this.money = 3;
      cards.put("Ратуша", 1);
      cards.put("Пекарня", 1);
      cards.put("Пшеничное поле", 1);
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
