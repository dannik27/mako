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
  private Map<String, CardHandler> cardHandlers = new HashMap<>();

  {
    Stream.of(
            CardHandler.simpleGreen("Пекарня", CardHandler.Type.BOX, 1, 1, List.of(2, 3)),
            CardHandler.simpleGreen("Магазин", CardHandler.Type.BOX, 2, 3, List.of(4)),

            CardHandler.complexGreen("Мебельная фабрика", CardHandler.Type.GEAR, cardHandlers, 3, 3, List.of(8)),

            CardHandler.simpleBlue("Wheat", CardHandler.Type.WHEAT, 1, 1, List.of(1)),
            CardHandler.simpleBlue("Farm", CardHandler.Type.PIG, 1, 1, List.of(2)),
            CardHandler.simpleBlue("Flowers", CardHandler.Type.WHEAT, 2, 1, List.of(4)),
            CardHandler.simpleBlue("Forest", CardHandler.Type.GEAR, 3, 1, List.of(5)),

            CardHandler.stadium(),

            CardHandler.yellow("Port", 2),
            CardHandler.yellow("Вокзал", 4),
            CardHandler.yellow("Shopping mall", 10),
            CardHandler.yellow("Theme park", 16),
            CardHandler.yellow("Radio tower", 22),
            CardHandler.yellow("Airport", 30)
        )
        .forEach(card -> cardHandlers.put(card.getName(), card));
  }


  public void requestState(String gameId) {
    log.info("User reconnected to game {}", gameId);
    notifier.notifyCards(gameId, cardHandlers.values());
    notifier.notifyGameState(sessions.get(gameId));
  }

  public void diceRoll(String gameId, String username, int diceCount) {
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
    if (diceCount == 2) {
      dice += new SecureRandom().nextInt(1, 7);
    }
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

      int finalDice = dice;
      player.getCards().forEach((cardName, count) -> {
        CardHandler cardHandler = cardHandlers.get(cardName);

        if (((cardHandler.getColor() == CardHandler.Color.GREEN && activePlayer == player) ||
            (cardHandler.getColor() == CardHandler.Color.BLUE) ||
            (cardHandler.getColor() == CardHandler.Color.RED && activePlayer != player) ||
            (cardHandler.getColor() == CardHandler.Color.PURPLE && activePlayer == player))
              && cardHandler.getNumbers().contains(finalDice)) {
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

    activePlayer.setLastBoughtCard(null);

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
    if (cardHandler == null) {
      log.error("Unable to find card {}", cardName);
    }

    if (cardHandler.getPrice() <= activePlayer.getMoney()) {
      if (activePlayer.getCards().containsKey(cardName)) {
        activePlayer.getCards().put(cardName, activePlayer.getCards().get(cardName) + 1);
      } else {
        activePlayer.getCards().put(cardName, 1);
      }
      activePlayer.setLastBoughtCard(cardName);
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
    notifier.notifyCards(game.getId(), cardHandlers.values());
    notifier.notifyGameState(state);
  }
}
