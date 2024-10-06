package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.model.CardHandler;

import java.util.List;
import java.util.Map;

public interface BotHandler {

  void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId);


  class MrBlue implements BotHandler {

    @Override
    public void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId) {
      GameStateResponse.PlayerStateDto player = state.getPlayers()
          .stream().filter(p -> p.getName().equals(username)).findFirst().get();

      if (state.getPhase().equals("CHOICE")) {

        if (state.getRequiredConfirmation().equals("Порт")) {
          gameSessionService.confirm(gameId, username, Map.of("name", "Порт", "addition", 0));
        }
        if (state.getRequiredConfirmation().equals("Радиовышка")) {
          gameSessionService.confirm(gameId, username, Map.of("name", "Радиовышка", "diceCount", 0));
        }

      } else if ( state.getPhase().equals("DICE") ) {

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        gameSessionService.diceRoll(gameId, username, player.getCards().containsKey("Вокзал") ? 2 : 1);
      } else if ( state.getPhase().equals("BUILD") ) {

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        List<GameSessionService.CardAndCount> availableCards = gameSessionService.getAvailableCards(gameId, username)
                .stream().filter(c -> c.count() > 0)
                .filter(c -> c.card().getColor() == CardHandler.Color.YELLOW || c.card().getColor() == CardHandler.Color.BLUE).toList();
        if (availableCards.isEmpty()) {
          gameSessionService.skipBuild(gameId, username);
        } else {
          CardHandler cardToBuy = availableCards.stream().sorted((a, b) -> {
            if (a.card().getColor() != b.card().getColor()) {
              if (state.getTurn() > 6) {
                return a.card().getColor() == CardHandler.Color.YELLOW ? 1 : -1;
              } else {
                return a.card().getColor() == CardHandler.Color.BLUE ? 1 : -1;
              }

            } else {
              return a.card().getPrice() - b.card().getPrice();
            }
          }).map(GameSessionService.CardAndCount::card).reduce((first, second) -> second).get();
          gameSessionService.buyCard(gameId, username, cardToBuy.getName());
        }
      }
    }
  }

  class MrBox implements BotHandler {

    @Override
    public void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId) {
      GameStateResponse.PlayerStateDto player = state.getPlayers()
          .stream().filter(p -> p.getName().equals(username)).findFirst().get();

      if ( state.getPhase().equals("DICE") ) {

        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        gameSessionService.diceRoll(gameId, username, 1);
      } else if ( state.getPhase().equals("BUILD") ) {

        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        List<GameSessionService.CardAndCount> availableCards = gameSessionService.getAvailableCards(gameId, username)
                .stream().filter(c -> c.count() > 0).toList();
        if (availableCards.isEmpty()) {
          gameSessionService.skipBuild(gameId, username);
        } else {
          CardHandler cardToBuy = availableCards.stream().sorted((a, b) -> {
            if (a.card().getColor() != b.card().getColor()) {
              return a.card().getColor() == CardHandler.Color.YELLOW ? 1 : 0;
            } else {
              return a.card().getPrice() - b.card().getPrice();
            }
          }).map(GameSessionService.CardAndCount::card).findFirst().get();
          gameSessionService.buyCard(gameId, username, cardToBuy.getName());
        }

      }
    }
  }
}
