package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.model.CardHandler;
import com.dannik.mako.model.GameState;
import lombok.RequiredArgsConstructor;

import java.util.*;

public interface BotHandler {

  void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId);


  @FunctionalInterface
  interface BotCondition {
    boolean check(GameStateResponse state);
  }

  static BotCondition buildPhase() {
    return (state) -> state.getPhase().equals("BUILD");
  }

  static BotCondition dicePhase() {
    return (state) -> state.getPhase().equals("DICE");
  }

  static BotCondition choicePhase() {
    return (state) -> state.getPhase().equals("CHOICE");
  }

  static BotCondition turnAfter(int turn) {
    return (state) -> state.getTurn() > turn;
  }

  static BotCondition hasCard(String cardName) {
    return (state) -> activePlayer(state).getCards().containsKey(cardName);
  }

  static BotCondition hasCard(String cardName, int number) {
    return (state) -> activePlayer(state).getCards().getOrDefault(cardName, 0) >= number;
  }

  static BotCondition not(BotCondition condition) {
    return (state -> !condition.check(state));
  }

  static BotCondition choiceTarget(String cardName) {
    return (state -> state.getRequiredConfirmation().equals(cardName));
  }

  static BotCondition always() {
    return (state -> true);
  }

  static BotCondition diceIn(Integer... diceValues) {
    return (state -> Arrays.asList(diceValues).contains(state.getLastDice()));
  }

  interface BotAction {
    boolean execute(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId);
  }

  static BotAction diceRoll(int diceCount) {
    return (state, service, user, gameId) -> {
      sleep(1000);
      service.diceRoll(gameId, user, diceCount);
      return true;
    };
  }

  static BotAction skipBuild() {
    return (state, service, user, gameId) -> {
      sleep(1000);
      service.skipBuild(gameId, user);
      return true;
    };
  }

  static BotAction buyCard(String... cards) {
    return (state, service, user, gameId) -> {
      List<String> availableCards = service.getAvailableCards(gameId, user).stream()
              .filter(c -> c.count() > 0)
              .map(c -> c.card().getName()).toList();

      for (String card: cards) {
        if (availableCards.contains(card)) {
          sleep(1000);
          service.buyCard(gameId, user, card);
          return true;
        }
      }
      return false;
    };
  }

  static BotAction doChoice(Map<String, Object> data) {
    return (state, service, user, gameId) -> {
      Map<String, Object> confirmation = new HashMap<>();
      confirmation.putAll(data);
      confirmation.put("name", state.getRequiredConfirmation());
      sleep(1000);
      service.confirm(gameId, user, confirmation);
      return true;
    };
  }

  static private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  static private GameStateResponse.PlayerStateDto activePlayer(GameStateResponse state) {
    return state.getPlayers().stream().filter(p -> p.getName().equals(state.getActivePlayer())).findFirst().get();
  }


  class Builder {

    record BotRule(List<BotCondition> conditions, BotAction action) {}

    private List<BotRule> botRules = new ArrayList<>();

    void addRule(List<BotCondition> conditions, BotAction action) {
      botRules.add(new BotRule(conditions, action));
    }

    public BotActionBuilder when(BotCondition... conditions) {
      return new BotActionBuilder(this, List.of(conditions));
    }

    public BotHandler build() {
      return new BotHandler() {
        @Override
        public void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId) {

          for (BotRule rule: botRules) {
            boolean condition = rule.conditions().stream().allMatch(c -> c.check(state));
            boolean executed = condition && rule.action().execute(state, gameSessionService, username, gameId);
            if (executed) {
              break;
            }
          }
        }
      };
    }

  }

  @RequiredArgsConstructor
  class BotActionBuilder {
    private final Builder builder;
    private final List<BotCondition> conditions;

    public Builder thenDo(BotAction action) {
      builder.addRule(conditions, action);
      return builder;
    }
  }

}
