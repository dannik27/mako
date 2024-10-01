package com.dannik.mako.services;

import com.dannik.mako.model.CardHandler;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import com.dannik.mako.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameSessionService {

  private final UserNotifier notifier;

  private Map<String, GameState> sessions = new HashMap<>();
  private Map<String, CardHandler> cardHandlers = Stream.of(new CardHandler.Bakery(), new CardHandler.Wheat(),
          new CardHandler.Forest())
      .collect(Collectors.toMap(CardHandler::getName, Function.identity()));



  public void requestState(String gameId) {
    log.info("User reconnected to game {}", gameId);
    notifier.notifyGameState(sessions.get(gameId));
  }

  public void diceRoll(String gameId, String username) {
    GameState state = sessions.get(gameId);
    if (state.getPhase() != GameState.GamePhase.DICE) {
      log.error("Wrong phase");
      return; //TODO: handle
    }

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayer());
    if (!activePlayer.getUser().getUsername().equals(username)) {
      log.error("It is not your turn: {} != {}", activePlayer.getUser().getUsername(), username);
      return;
    }

    int dice = new SecureRandom().nextInt(1, 7);
    state.setLastDice(dice);


    for (GameState.PlayerState player: state.getPlayers()) {
      int index = state.getPlayers().indexOf(player);

      player.getLastMoneyChange().removeIf(all -> true);
      List<GameState.PlayerState> opponentsBefore = new ArrayList<>();
      for ( int i = index - 1; i >= 0; i-- ) {
        opponentsBefore.add(state.getPlayers().get(i));
      }
      for ( int i = state.getPlayers().size() - 1; i > index; i-- ) {
        opponentsBefore.add(state.getPlayers().get(i));
      }

      player.getCards().forEach((cardName, count) -> {
        CardHandler cardHandler = cardHandlers.get(cardName);

        if (((cardHandler.getColor() == CardHandler.Color.GREEN && activePlayer == player) ||
            (cardHandler.getColor() == CardHandler.Color.BLUE) ||
            (cardHandler.getColor() == CardHandler.Color.RED && activePlayer != player) ||
            (cardHandler.getColor() == CardHandler.Color.PURPLE && activePlayer == player))
              && cardHandler.getNumbers().contains(dice)) {
          cardHandler.handle(player, opponentsBefore, count);
        }

      });

    }

    state.setPhase(GameState.GamePhase.BUILD);
    notifier.notifyGameState(state);
    //TODO: handle cards with confirmation

  }

  public void skipBuild(String gameId, String username) {
    GameState state = sessions.get(gameId);
    if (state.getPhase() != GameState.GamePhase.BUILD) {
      log.error("Wrong phase");
      return; //TODO: handle
    }

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayer());
    if (!activePlayer.getUser().getUsername().equals(username)) {
      log.error("It is not your turn: {} != {}", activePlayer.getUser().getUsername(), username);
      return;
    }

    if (state.getActivePlayer() < state.getPlayers().size() - 1) {
      state.setActivePlayer(state.getActivePlayer() + 1);
    } else {
      state.setActivePlayer(0);
    }

    state.setPhase(GameState.GamePhase.DICE);
    notifier.notifyGameState(state);
  }

  public void buyCard(String gameId, String username, String cardName) {
    GameState state = sessions.get(gameId);
    if (state.getPhase() != GameState.GamePhase.BUILD) {
      log.error("Wrong phase");
      return; //TODO: handle
    }

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayer());
    if (!activePlayer.getUser().getUsername().equals(username)) {
      log.error("It is not your turn: {} != {}", activePlayer.getUser().getUsername(), username);
      return;
    }

    CardHandler cardHandler = cardHandlers.get(cardName);

    if (cardHandler.getPrice() <= activePlayer.getMoney()) {
      if (activePlayer.getCards().containsKey(cardName)) {
        activePlayer.getCards().put(cardName, activePlayer.getCards().get(cardName) + 1);
      } else {
        activePlayer.getCards().put(cardName, 1);
      }
      activePlayer.setMoney(activePlayer.getMoney() - cardHandler.getPrice());
    } else {
      //TODO: no money
      return;
    }

    if (state.getActivePlayer() < state.getPlayers().size() - 1) {
      state.setActivePlayer(state.getActivePlayer() + 1);
    } else {
      state.setActivePlayer(0);
    }

    state.setPhase(GameState.GamePhase.DICE);
    notifier.notifyGameState(state);

  }

  public void init(Game game) {
    GameState state = GameState.init(game);
    sessions.put(game.getId(), state);
    notifier.notifyGameState(state);
  }
}
