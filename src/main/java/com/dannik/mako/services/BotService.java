package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BotService {

  private final UserRepository userRepository;
  @Lazy
  private final GameSessionService sessionService;

  private final Map<String, BotHandler> bots = new HashMap<>();

  @PostConstruct
  public void init() {
    bots.put("MrBlue", new BotHandler.MrBlue());
    bots.put("MrBox", new BotHandler.MrBox());
    userRepository.create("MrBlue", true);
    userRepository.create("MrBox", true);
  }

  public void notifyBots(GameStateResponse gameState, String gameId) {
    String activePlayerName = gameState.getActivePlayer();
    BotHandler handler = bots.get(activePlayerName);
    if (handler == null) {
      return;
    }
    handler.handle(gameState, sessionService, activePlayerName, gameId);
  }

}
