package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;

public interface BotHandler {

  void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId);


  class MrBlue implements BotHandler {

    @Override
    public void handle(GameStateResponse state, GameSessionService gameSessionService, String username, String gameId) {
      if ( state.getPhase().equals("DICE") ) {
        gameSessionService.diceRoll(gameId, username);
      } else if ( state.getPhase().equals("BUILD") ) {
        GameStateResponse.PlayerStateDto player = state.getPlayers()
            .stream().filter(p -> p.getName().equals(username)).findFirst().get();
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
}
