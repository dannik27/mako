package com.dannik.mako.model;

import lombok.*;

import java.util.*;

@Data
public class GameState {

  private Game game;
  private List<PlayerState> players;

  private int activePlayerIndex;
  private GamePhase phase;
  private int lastDice;
  private String lastBuilding;
  private int turn = 1;
  private boolean wasDouble = false;

  private Map<String, Map<String, Object>> confirmations = new HashMap<>();
  private String requiredConfirmation;

  public PlayerState getActivePlayer() {
    return players.get(activePlayerIndex);
  }

  public void setNextPlayer(EventNotifier notifier) {
    if (getActivePlayerIndex() < getPlayers().size() - 1) {
      setActivePlayerIndex(getActivePlayerIndex() + 1);
    } else {
      setActivePlayerIndex(0);
      setTurn(turn + 1);
      players.forEach(p -> p.fundUsed = false);
      notifier.notify("Ход " + turn);
    }
  }

  public static GameState init(Game game, List<CardHandler> startCards) {
    GameState state = new GameState();
    state.game = game;
    state.players = game.getPlayers().stream().map(user -> new PlayerState(user, startCards)).toList();
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
    private List<CardState> cards = new ArrayList<>();
    private List<MoneyChange> lastMoneyChange = new ArrayList<>();
    private List<MoneyChange> moneyChangeHistory = new ArrayList<>();
    private String lastBoughtCard;
    private boolean left = false;

    private boolean fundUsed = false;
    private int fundValue = 0;

    public String getName() {
      return getUser().getUsername();
    }

    public void addToFund() {
      fundValue += 1;
      fundUsed = true;
    }

    public boolean hasCard(String cardName) {
      return cards.stream().anyMatch(c -> c.getHandler().getName().equals(cardName));
    }

    public int getCardsCount(String cardName) {
      return cards.stream().filter(c -> c.getHandler().getName().equals(cardName))
          .map(CardState::getCount).findFirst().orElse(0);
    }

    public void addCard(CardHandler handler) {
      cards.stream().filter(c -> c.getHandler().equals(handler)).findFirst()
          .ifPresentOrElse(card -> card.count += 1, () -> cards.add(new CardState(handler, 1, 0)));
    }

    public void addMoneyChange(String cardName, int count, String opponent) {
      MoneyChange moneyChange = new MoneyChange(cardName, count, opponent);
      lastMoneyChange.add(moneyChange);
      moneyChangeHistory.add(moneyChange);
    }

    public PlayerState(User user, List<CardHandler> startCards) {
      this.user = user;
      this.money = 3;
      startCards.forEach(handler -> {
        cards.add(new CardState(handler, 1, 0));
      });
    }

  }

  @Data
  @RequiredArgsConstructor
  public static class MoneyChange {
    private final String card;
    private final int count;
    private final String opponent;
  }

  @Data
  @AllArgsConstructor
  public static class CardState {
    private CardHandler handler;
    private int count;
    private int disabledCount;

    public String getName() {
      return handler.getName();
    }
  }

}
