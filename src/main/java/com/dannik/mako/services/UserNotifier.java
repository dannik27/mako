package com.dannik.mako.services;

import com.dannik.mako.messages.GamesUpdate;
import com.dannik.mako.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserNotifier {

  private final SimpMessageSendingOperations messagingTemplate;

  public void notifyGameUpdate(Game game) {
    messagingTemplate.convertAndSend("/topic/games/update", GamesUpdate.of(game));
  }

}
