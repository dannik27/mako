package com.dannik.mako.services;

import com.dannik.mako.model.CardHandler;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import com.dannik.mako.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dannik.mako.model.CardHandler.Color.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameSessionService {

  private final UserNotifier notifier;

  @Lazy
  private final GameService gameService;

  private Map<String, GameState> sessions = new HashMap<>();
  private Map<String, CardHandler> cardHandlers = new HashMap<>();

  {
    Stream.of(

            CardHandler.Builder.create("Универсам", CardHandler.Type.BOX, 0)
                .simpleGreenBlue(GREEN, List.of(2), 2)
                .sightsRequired(1).build(),
            CardHandler.Builder.create("Пекарня", CardHandler.Type.BOX, 1)
                .simpleGreenBlue(GREEN, List.of(2, 3), 1)
                .startCard(true).build(),
            CardHandler.Builder.create("Магазин", CardHandler.Type.BOX, 2)
                .simpleGreenBlue(GREEN, List.of(4), 3).build(),
            CardHandler.Builder.create("Банк", CardHandler.Type.BUSINESS, 0)
                .simpleGreenBlue(GREEN, List.of(4), -2)
                .afterBuild(player -> player.setMoney(player.getMoney() + 5)).build(),
            // 4 demontaj

            CardHandler.Builder.create("Цветочный магазин", CardHandler.Type.BOX, 1)
                .complexGreen(List.of(6), 1, c -> c.getName().equals("Цветник")).build(),
            CardHandler.Builder.create("Сыроварня", CardHandler.Type.FACTORY, 5)
                .complexGreen(List.of(7), 3, c -> c.getType() == CardHandler.Type.PIG).build(),
            CardHandler.Builder.create("Мебельная фабрика", CardHandler.Type.FACTORY, 3)
                .complexGreen(List.of(8), 3, c -> c.getType() == CardHandler.Type.GEAR).build(),
            // vinni zavod
          // 9-10 transport company
            CardHandler.Builder.create("Завод напитков", CardHandler.Type.FACTORY, 5)
                .forEveryCard(List.of(11), 1, c -> c.getType() == CardHandler.Type.CUP).build(),
            CardHandler.Builder.create("Фруктовый рынок", CardHandler.Type.FRUIT, 2)
                .complexGreen(List.of(11, 12), 2, c -> c.getType() == CardHandler.Type.WHEAT).build(),
            CardHandler.Builder.create("Склад продовольствия", CardHandler.Type.FACTORY, 2)
                .complexGreen(List.of(12, 13), 2, c -> c.getType() == CardHandler.Type.CUP).build(),


            CardHandler.Builder.create("Пшеничное поле", CardHandler.Type.WHEAT, 1)
                .simpleGreenBlue(BLUE, List.of(1), 1)
                .startCard(true).build(),
            CardHandler.Builder.create("Ферма", CardHandler.Type.PIG, 1)
                .simpleGreenBlue(BLUE, List.of(2), 1).build(),
            CardHandler.Builder.create("Кукурузное поле", CardHandler.Type.WHEAT, 2)
                .simpleGreenBlue(BLUE, List.of(3, 4), 1)
                .sightsRequired(1).build(),
            CardHandler.Builder.create("Цветник", CardHandler.Type.WHEAT, 2)
                .simpleGreenBlue(BLUE, List.of(4), 1).build(),
            CardHandler.Builder.create("Лес", CardHandler.Type.GEAR, 3)
                .simpleGreenBlue(BLUE, List.of(5), 1).build(),
            CardHandler.Builder.create("Виноградник", CardHandler.Type.WHEAT, 3)
                .simpleGreenBlue(BLUE, List.of(7), 3).build(),
            CardHandler.Builder.create("Рыбацкий баркас", CardHandler.Type.SHIP, 2)
                .simpleGreenBlue(BLUE, List.of(8), 3)
                .requiredCard("Порт").build(),
            CardHandler.Builder.create("Шахта", CardHandler.Type.GEAR, 6)
                .simpleGreenBlue(BLUE, List.of(9), 5).build(),
            CardHandler.Builder.create("Яблоневый сад", CardHandler.Type.WHEAT, 3)
                .simpleGreenBlue(BLUE, List.of(10), 3).build(),
            // 12-14 trauler

            CardHandler.Builder.create("Стадион", CardHandler.Type.ADMIN, 6)
                .purple(List.of(6), CardHandler.stadium).build(),
            CardHandler.Builder.create("Телецентр", CardHandler.Type.ADMIN, 7)
                .purple(List.of(6), CardHandler.tvCenter).build(),
            // Деловой центр
            CardHandler.Builder.create("Издательство", CardHandler.Type.ADMIN, 5)
                .purple(List.of(7), CardHandler.publishing).build(),
            // 8 Клининговая компания
            // 8-9 Налоговая инспекция
            // 10 Венчурный фонд
            // 11 13 Парк

            // susi
        // kafe
        // 5 pres restoran
        // 7 pizza
        // 8 zakuska
        // 9-10 restoran
        // 12-14 bar

            CardHandler.Builder.create("Ратуша", CardHandler.Type.ADMIN, -1)
                .yellow().startCard(true).build(), //ok
            CardHandler.Builder.create("Порт", CardHandler.Type.ADMIN, 2)
                .yellow(List.of(10, 11, 12)).withConfirmation(true).build(), //ok
            CardHandler.Builder.create("Вокзал", CardHandler.Type.ADMIN, 4).yellow().build(), //ok
            CardHandler.Builder.create("Торговый центр", CardHandler.Type.ADMIN, 10).yellow().build(), //ok
            CardHandler.Builder.create("Парк развлечений", CardHandler.Type.ADMIN, 16).yellow().build(), //ok
            CardHandler.Builder.create("Радиовышка", CardHandler.Type.ADMIN, 22).yellow()
                .withConfirmation(true).build(), //ok
            CardHandler.Builder.create("Аэропорт", CardHandler.Type.ADMIN, 30).yellow().build() //ok
        )
        .forEach(card -> cardHandlers.put(card.getName(), card));
  }


  public void requestState(String gameId) {
    log.info("User reconnected to game {}", gameId);
    notifier.notifyCards(gameId, cardHandlers.values());
    notifier.notifyGameState(sessions.get(gameId));
  }

  public void confirm(String gameId, String username, Map<String, Object> confirmation) {
    String cardName = (String) confirmation.get("name");
    GameState state = sessions.get(gameId);
    if (state.getPhase() != GameState.GamePhase.CHOICE) {
      log.error("Wrong phase");
      return; //TODO: handle
    }

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayer());
    if (!activePlayer.getUser().getUsername().equals(username)) {
      log.error("It is not your turn: {} != {}", activePlayer.getUser().getUsername(), username);
      return;
    }

    state.getConfirmations().put(cardName, confirmation);
    if (cardName.equals("Порт")) {
      int lastDice = state.getLastDice();
      lastDice += ((int) state.getConfirmations().get("Порт").get("addition"));
      state.setLastDice(lastDice);
    }

    if (cardName.equals("Радиовышка")) {
      int diceCount = (int) confirmation.get("diceCount");
      if (diceCount > 0) {
        state.setPhase(GameState.GamePhase.DICE);
        diceRoll(gameId, username, diceCount);
        return;
      }
    }

    calculateIncomes(state, activePlayer);
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

    state.setWasDouble(false);
    int dice = new SecureRandom().nextInt(1, 7);
    if (diceCount == 2) {
      int secondDice = new SecureRandom().nextInt(1, 7);
      if (dice == secondDice) {
        state.setWasDouble(true);
      }
      dice += secondDice;
    }
    state.setLastDice(dice);

    calculateIncomes(state, activePlayer);
  }

  public void calculateIncomes(GameState state, GameState.PlayerState activePlayer) {
    for (GameState.CardState card : activePlayer.getCards()) {

      if (card.getHandler().isConfirmationRequired(state.getLastDice()) && !state.getConfirmations().containsKey(card.getName())) {
        state.setRequiredConfirmation(card.getName());
        state.setPhase(GameState.GamePhase.CHOICE);
        notifier.notifyGameState(state);
        return;
      }
    }

    for (GameState.PlayerState player : state.getPlayers()) {
      int index = state.getPlayers().indexOf(player);

      player.getLastMoneyChange().removeIf(all -> true);
      List<GameState.PlayerState> opponentsBefore = new ArrayList<>();
      for (int i = index - 1; i >= 0; i--) {
        opponentsBefore.add(state.getPlayers().get(i));
      }
      for (int i = state.getPlayers().size() - 1; i > index; i--) {
        opponentsBefore.add(state.getPlayers().get(i));
      }

      int dice = state.getLastDice();
      player.getCards().forEach((cardState) -> {
        CardHandler cardHandler = cardState.getHandler();

        if (((cardHandler.getColor() == GREEN && activePlayer == player) ||
            (cardHandler.getColor() == BLUE) ||
            (cardHandler.getColor() == CardHandler.Color.RED && activePlayer != player) ||
            (cardHandler.getColor() == PURPLE && activePlayer == player))
            && cardHandler.getNumbers().contains(dice)) {
          cardHandler.handle(player, opponentsBefore, state.getConfirmations(), cardState.getCount());
        }

      });
    }

    if (activePlayer.hasCard("Ратуша") && activePlayer.getMoney() == 0) {
      activePlayer.setMoney(1);
    }

    state.setConfirmations(new HashMap<>());
    state.setPhase(GameState.GamePhase.BUILD);
    notifier.notifyGameState(state);
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

    if (activePlayer.hasCard("Аэропорт")) {
      activePlayer.setMoney(activePlayer.getMoney() + 10);
    }

    if (!activePlayer.hasCard("Парк развлечений") || !state.isWasDouble()) {
      if (state.getActivePlayer() < state.getPlayers().size() - 1) {
        state.setActivePlayer(state.getActivePlayer() + 1);
      } else {
        state.setActivePlayer(0);
      }
    }

    state.setTurn(state.getTurn() + 1);

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
      activePlayer.addCard(cardHandler);
      activePlayer.setLastBoughtCard(cardName);
      activePlayer.setMoney(activePlayer.getMoney() - cardHandler.getPrice());
    } else {
      //TODO: no money
      log.error("No money");
      return;
    }

    long yellowBought = activePlayer.getCards().stream().map(GameState.CardState::getHandler)
        .filter(h -> h.getColor() == CardHandler.Color.YELLOW).count();
    long yellowNeeded = cardHandlers.values().stream().filter(h -> h.getColor() == CardHandler.Color.YELLOW).count();

    if (yellowBought == yellowNeeded) {
      state.setPhase(GameState.GamePhase.WINNER);
      gameService.endGame(gameId, activePlayer.getUser());
      notifier.notifyWinner(state, username);

    } else {
      if (!activePlayer.hasCard("Парк развлечений") || !state.isWasDouble()) {
        if (state.getActivePlayer() < state.getPlayers().size() - 1) {
          state.setActivePlayer(state.getActivePlayer() + 1);
        } else {
          state.setActivePlayer(0);
        }
      }
      state.setTurn(state.getTurn() + 1);
      state.setPhase(GameState.GamePhase.DICE);
      notifier.notifyGameState(state);
    }


  }

  public record CardAndCount(CardHandler card, int count) {
  }

  public List<CardAndCount> getAvailableCards(String gameId, String username) {

    GameState state = sessions.get(gameId);
    GameState.PlayerState player = state.getPlayers().stream().filter(p -> p.getUser().getUsername().equals(username)).findFirst()
        .orElseThrow(() -> new RuntimeException("Player not found"));

    return cardHandlers.values().stream().map(card -> {
      if (card.getPrice() < 0) {
        return new CardAndCount(card, 0);

      } else if (player.getMoney() < card.getPrice()) {
        return new CardAndCount(card, 0);

      } else if (card.getColor() == PURPLE || card.getColor() == CardHandler.Color.YELLOW) {
        int count = player.hasCard(card.getName())
            ? 0
            : 1;
        return new CardAndCount(card, count);
      } else {
        int occupied = state.getPlayers().stream()
            .mapToInt(p -> p.getCardsCount(card.getName())).sum();

        int count = card.isStartCard()
            ? 6 - (occupied - state.getPlayers().size())
            : 6 - occupied;
        return new CardAndCount(card, count);
      }
    }).toList();
  }

  public void init(Game game) {
    GameState state = GameState.init(game, cardHandlers.values().stream().filter(CardHandler::isStartCard).toList());
    sessions.put(game.getId(), state);
    notifier.notifyCards(game.getId(), cardHandlers.values());
    notifier.notifyGameState(state);
  }
}
