package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.messages.GamesUpdate;
import com.dannik.mako.model.CardHandler;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import com.dannik.mako.model.GameStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.dannik.mako.model.CardHandler.Color.YELLOW;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotifier {

  private final SimpMessageSendingOperations messagingTemplate;
  private final BotService botService;

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
    GameStateResponse response = GameStateResponse.of(state);
    messagingTemplate.convertAndSend("/topic/game/%s/state".formatted(gameId), response);
    botService.notifyBots(response, gameId);
  }

  public void notifyWinner(GameState state, String winner) {
    String gameId = state.getGame().getId();
    log.info("Notify game winner: {}", gameId);
    GameStateResponse response = GameStateResponse.of(state, winner);
    messagingTemplate.convertAndSend("/topic/game/%s/state".formatted(gameId), response);
    botService.notifyBots(response, gameId);
  }

  record CardInfo(String name, String color, int price, List<Integer> numbers, boolean startCard) {}

  public void notifyCards(String gameId, Collection<CardHandler> cards) {
    List<CardInfo> cardInfo = cards.stream().filter(c -> c.getPrice() >= 0)
        .map(c -> new CardInfo(c.getName(), c.getColor().name(), c.getPrice(), c.getColor() != YELLOW ? c.getNumbers() : null, c.isStartCard())).toList();

    messagingTemplate.convertAndSend("/topic/game/%s/cards".formatted(gameId), cardInfo);
  }

  public void notifyEvent(String gameId, String message) {
    messagingTemplate.convertAndSend("/topic/game/%s/event".formatted(gameId), message);
  }

}
