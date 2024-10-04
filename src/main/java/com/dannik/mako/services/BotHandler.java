package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;

public interface BotHandler {

  void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId);


  class MrBlue implements BotHandler {

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

        gameSessionService.diceRoll(gameId, username, player.getCards().containsKey("Вокзал") ? 2 : 1);
      } else if ( state.getPhase().equals("BUILD") ) {

        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        int money = player.getMoney();
        if (money >= 3) {
          gameSessionService.buyCard(gameId, username, "Forest");
        } else if (money >= 1) {
          gameSessionService.buyCard(gameId, username, "Wheat");
        } else {
          gameSessionService.skipBuild(gameId, username);
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
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        int money = player.getMoney();
        if (money >= 2) {
          gameSessionService.buyCard(gameId, username, "Магазин");
        } else if (money >= 1) {
          gameSessionService.buyCard(gameId, username, "Пекарня");
        } else {
          gameSessionService.skipBuild(gameId, username);
        }
      }
    }
  }
}
