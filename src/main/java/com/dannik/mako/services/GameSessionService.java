package com.dannik.mako.services;

import com.dannik.mako.model.CardHandler;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;
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

            CardHandler.Builder.create("Универсам", CardHandler.Type.BOX, 0) //ok
                .simpleGreenBlue(GREEN, List.of(2), 2)
                .sightsRequired(1).build(),
            CardHandler.Builder.create("Пекарня", CardHandler.Type.BOX, 1) //ok
                .simpleGreenBlue(GREEN, List.of(2, 3), 1)
                .startCard(true).build(),
            CardHandler.Builder.create("Магазин", CardHandler.Type.BOX, 2) //ok
                .simpleGreenBlue(GREEN, List.of(4), 3).build(),
            CardHandler.Builder.create("Банк", CardHandler.Type.BUSINESS, 0) //ok
                .simpleGreenBlue(GREEN, List.of(4), -2) // ok
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
            CardHandler.Builder.create("Налоговая инспекция", CardHandler.Type.ADMIN, 4)
                .purple(List.of(8, 9), CardHandler.nalogovaya).build(),
            // 10 Венчурный фонд
            // 11 13 Парк

            CardHandler.Builder.create("Суси-бар", CardHandler.Type.CUP, 2)
                    .simpleRed(List.of(1), 3).requiredCard("Порт").build(),
            CardHandler.Builder.create("Кафе", CardHandler.Type.CUP, 2)
                .simpleRed(List.of(3), 1).build(),
            CardHandler.Builder.create("Престижный ресторан", CardHandler.Type.CUP, 3)
                .simpleRed(List.of(5), 5).sightsRequired(2).build(),
            CardHandler.Builder.create("Пиццерия", CardHandler.Type.CUP, 1)
                .simpleRed(List.of(7), 1).build(),
            CardHandler.Builder.create("Закусочная", CardHandler.Type.CUP, 1)
                .simpleRed(List.of(8), 1).build(),
            CardHandler.Builder.create("Ресторан", CardHandler.Type.CUP, 3)
                .simpleRed(List.of(9, 10), 2).build(),
            CardHandler.Builder.create("Частный бар", CardHandler.Type.CUP, 4)
                .simpleRed(List.of(12, 14), 1000).build(),

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

  public void leaveGame(String gameId, String username) {

    GameState state = sessions.get(gameId);
    GameState.PlayerState player = state.getPlayers().stream().filter(p -> p.getUser().getUsername().equals(username))
        .findFirst().get();
    player.setLeft(true);
    if (state.getActivePlayer() == player) {
      state.setNextPlayer();
    }
    gameService.getActiveGame(username).get().getPlayers().remove(player.getUser());//todo: mb refactor

    notifier.notifyGameState(state);
  }

  public void confirm(String gameId, String username, Map<String, Object> confirmation) {
    String cardName = (String) confirmation.get("name");
    GameState state = sessions.get(gameId);
    if (state.getPhase() != GameState.GamePhase.CHOICE) {
      log.error("Wrong phase");
      return; //TODO: handle
    }

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayerIndex());
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

    calculateIncomes(gameId, state, activePlayer);
  }

  public void diceRoll(String gameId, String username, int diceCount) {
    GameState state = sessions.get(gameId);
    if (state.getPhase() != GameState.GamePhase.DICE) {
      log.error("Wrong phase");
      return; //TODO: handle
    }

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayerIndex());
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
      notifier.notifyEvent(gameId, "Игрок %s бросил два кубика: %d - %d".formatted(username, dice, secondDice));
      dice += secondDice;
    } else {
      notifier.notifyEvent(gameId, "Игрок %s бросил кубик: %d".formatted(username, dice));
    }
    state.setLastDice(dice);

    calculateIncomes(gameId, state, activePlayer);
  }

  public void calculateIncomes(String gameId, GameState state, GameState.PlayerState activePlayer) {
    for (GameState.CardState card : activePlayer.getCards()) {

      if (card.getHandler().isConfirmationRequired(state.getLastDice()) && !state.getConfirmations().containsKey(card.getName())) {
        state.setRequiredConfirmation(card.getName());
        state.setPhase(GameState.GamePhase.CHOICE);
        notifier.notifyGameState(state);
        return;
      }
    }

    int activePlayerIndex = state.getActivePlayerIndex();
    List<GameState.PlayerState> opponentsBefore = new ArrayList<>();
    for (int i = activePlayerIndex - 1; i >= 0; i--) {
      opponentsBefore.add(state.getPlayers().get(i));
    }
    for (int i = state.getPlayers().size() - 1; i > activePlayerIndex; i--) {
      opponentsBefore.add(state.getPlayers().get(i));
    }

    for (GameState.PlayerState player : state.getPlayers()) {
      player.getLastMoneyChange().removeIf(all -> true);
    }
    int dice = state.getLastDice();
    long activePlayerSights = activePlayer.getCards().stream().filter(c -> c.getHandler().getColor() == YELLOW).count();

    for (GameState.PlayerState opponent: opponentsBefore) {
      opponent.getCards().stream()
          .filter(c -> c.getHandler().getColor() == RED)
          .filter(c -> c.getHandler().getNumbers().contains(dice))
          .filter(c -> c.getHandler().sightsRequired() == 0 || activePlayerSights >= c.getHandler().sightsRequired())
          .filter(c -> c.getHandler().getRequiredCard() == null || opponent.getCards().stream().anyMatch(cc -> cc.getHandler().getName().equals(c.getHandler().getRequiredCard())))
          .forEach(cardState -> {
            cardState.getHandler().handle(opponent, activePlayer, null, state.getConfirmations(), cardState.getCount(),
                (message) -> notifier.notifyEvent(gameId, message));
          });
    }

    for (GameState.PlayerState player: state.getPlayers()) {
      player.getCards().stream()
              .filter(c -> c.getHandler().getColor() == BLUE )
              .filter(c -> c.getHandler().getNumbers().contains(dice))
              .filter(c -> c.getHandler().sightsRequired() == 0 || activePlayerSights <= c.getHandler().sightsRequired())
              .filter(c -> c.getHandler().getRequiredCard() == null || player.getCards().stream().anyMatch(cc -> cc.getHandler().getName().equals(c.getHandler().getRequiredCard())))
              .forEach(cardState -> {
                cardState.getHandler().handle(player, activePlayer, opponentsBefore, state.getConfirmations(), cardState.getCount(),
                        (message) -> notifier.notifyEvent(gameId, message));
              });
    }

    activePlayer.getCards().stream()
        .filter(c -> c.getHandler().getColor() == GREEN || c.getHandler().getColor() == PURPLE)
        .filter(c -> c.getHandler().getNumbers().contains(dice))
        .filter(c -> c.getHandler().sightsRequired() == 0 || activePlayerSights <= c.getHandler().sightsRequired())
            .filter(c -> c.getHandler().getRequiredCard() == null || activePlayer.getCards().stream().anyMatch(cc -> cc.getHandler().getName().equals(c.getHandler().getRequiredCard())))
        .forEach(cardState -> {
          cardState.getHandler().handle(activePlayer, activePlayer, opponentsBefore, state.getConfirmations(), cardState.getCount(),
              (message) -> notifier.notifyEvent(gameId, message));
        });

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

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayerIndex());
    if (!activePlayer.getUser().getUsername().equals(username)) {
      log.error("It is not your turn: {} != {}", activePlayer.getUser().getUsername(), username);
      return;
    }

    activePlayer.setLastBoughtCard(null);

    if (activePlayer.hasCard("Аэропорт")) {
      activePlayer.setMoney(activePlayer.getMoney() + 10);
    }

    if (!activePlayer.hasCard("Парк развлечений") || !state.isWasDouble()) {
      state.setNextPlayer();
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

    GameState.PlayerState activePlayer = state.getPlayers().get(state.getActivePlayerIndex());
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
      cardHandler.doAfterBuild(activePlayer);
      notifier.notifyEvent(gameId, "Игрок %s купил карту %s".formatted(activePlayer.getUser().getUsername(), cardName));
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
      notifier.notifyEvent(gameId, "Игрок %s победил. Ход %s".formatted(activePlayer.getUser().getUsername(), state.getTurn()));
      notifier.notifyWinner(state, username);

    } else {
      if (cardName.equals("Парк развлечений") || !activePlayer.hasCard("Парк развлечений") || !state.isWasDouble()) { //todo: if park bought now
        state.setNextPlayer();
      }

      state.setPhase(GameState.GamePhase.DICE);
      notifier.notifyGameState(state);
    }


  }

  public Optional<GameState> findActiveGameByUser(String username) {
    return sessions.values().stream()
        .filter(game -> game.getPlayers().stream().anyMatch(p -> p.getUser().getUsername().equals(username)))
        .findFirst();
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
