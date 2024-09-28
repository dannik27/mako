package com.dannik.mako.services;

import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameSessionService {

  private final UserNotifier notifier;

  private Map<String, GameState> sessions = new HashMap<>();


  public void requestState(String gameId) {
    log.info("User reconnected to game {}", gameId);
    notifier.notifyGameState(sessions.get(gameId));
  }

  public void init(Game game) {
    GameState state = GameState.init(game);
    sessions.put(game.getId(), state);
    notifier.notifyGameState(state);
  }
}
