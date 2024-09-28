package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.messages.GamesUpdate;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import com.dannik.mako.model.GameStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotifier {

  private final SimpMessageSendingOperations messagingTemplate;

  public void notifyGameUpdate(Game game) {
    log.info("Notify game update: {}", game.getId());
    messagingTemplate.convertAndSend("/topic/games/update", GamesUpdate.of(game));
  }

  public void notifyGameDelete(Game game) {
    log.info("Notify game delete: {}", game.getId());
    messagingTemplate.convertAndSend("/topic/games/update", GamesUpdate.deletedOf(game));
  }

  public void notifyGameState(GameState state) {
    String gameId = state.getGame().getId();
    log.info("Notify game state: {}", gameId);
    messagingTemplate.convertAndSend("/topic/game/%s/state".formatted(gameId), GameStateResponse.of(state));
  }

}
