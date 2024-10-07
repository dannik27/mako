package com.dannik.mako.model;

import com.dannik.mako.services.UserNotifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.dannik.mako.model.CardHandler.Type.*;


public interface CardHandler {

  Color getColor();

  Type getType();

  String getName();

  void handle(GameState.PlayerState player, GameState.PlayerState activePlayer, List<GameState.PlayerState> opponents,
              Map<String, Map<String, Object>> confirmations, int count, Notifier notifier);

  static void add(String cardName, Type cardType, GameState.PlayerState player, int count, Notifier notifier) {
    int finalCount = count;
    if (finalCount < 0) { // bank
      int minus = Math.min(player.getMoney(), -finalCount);
      player.setMoney(player.getMoney() - minus);
      player.getLastMoneyChange().add(new GameState.MoneyChange(cardName, -minus, "bank"));
      notifier.notify("Игрок %s заплатил %d монет за карту %s".formatted(player.getUser().getUsername(), minus, cardName));
    } else {
      player.setMoney(player.getMoney() + finalCount);
      player.getLastMoneyChange().add(new GameState.MoneyChange(cardName, finalCount, null));
      notifier.notify("Игрок %s получил %d монет за карту %s".formatted(player.getUser().getUsername(), finalCount, cardName));
    }
  }

  static int remove(String cardName, GameState.PlayerState fromWhom, int count, GameState.PlayerState toWhom, Notifier notifier) {
    if (fromWhom.getMoney() == 0) {
      return 0;
    }
    int minus = Math.min(fromWhom.getMoney(), count);
    fromWhom.setMoney(fromWhom.getMoney() - minus);
    fromWhom.getLastMoneyChange().add(new GameState.MoneyChange(cardName, -minus, toWhom.getUser().getUsername()));
    notifier.notify("Игрок %s заплатил %d монет игроку %s за карту %s".formatted(fromWhom.getUser().getUsername(),
        minus, toWhom.getUser().getUsername(), cardName));
    return minus;
  }



  List<Integer> getNumbers();

  int getPrice();

  boolean isStartCard();

  int sightsRequired();

  boolean isConfirmationRequired(int dice);

  void doAfterBuild(GameState.PlayerState player);

  enum Color {
    BLUE, GREEN, RED, PURPLE, YELLOW
  }

  enum Type {
    WHEAT, PIG, GEAR, BOX, ADMIN, FACTORY, CUP, BUSINESS, SHIP, FRUIT
  }

  @FunctionalInterface
  interface Notifier {
    void notify(String message);
  }

  @FunctionalInterface
  interface HandlerFunction {
    void handle(GameState.PlayerState player, GameState.PlayerState activePlayer, List<GameState.PlayerState> opponents, Map<String, Map<String, Object>> confirmations, int count, Notifier notifier);
  }

  @FunctionalInterface
  interface AfterBuildFunction {
    void handle(GameState.PlayerState player);
  }

  HandlerFunction stadium = (player, activePlayer, opponents, confirmations, count, notifier) -> {
    int sum = 0;
    for (GameState.PlayerState opponent : opponents) {
      sum += remove("Стадион", opponent, count * 2, player, notifier);
    }
    if (sum > 0) {
      add("Стадион", ADMIN, player, sum, notifier);
    }
  };

  HandlerFunction tvCenter = (player, activePlayer, opponents, confirmations, count, notifier) -> {
    String targetName = (String) confirmations.get("Телецентр").get("opponent");
    GameState.PlayerState target = opponents.stream().filter(op -> op.getUser().getUsername().equals(targetName)).findFirst()
        .orElseThrow();
    int removed = remove("Телецентр", target, 5, player, notifier);
    add("Телецентр", ADMIN, player, removed, notifier);
  };

  HandlerFunction publishing = (player, activePlayer, opponents, confirmations, count, notifier) -> {
    int sum = 0;
    for (GameState.PlayerState opponent : opponents) {
      int cardsCount = opponent.getCards().stream()
          .filter(c -> c.getHandler().getType() == CUP || c.getHandler().getType() == BOX)
          .mapToInt(GameState.CardState::getCount).sum();
      sum += remove("Издательство", opponent, cardsCount, player, notifier);
    }
    if (sum > 0) {
      add("Издательство", ADMIN, player, sum, notifier);
    }
  };

  class Builder {

    private String name;
    private Color color;
    private Type type;
    private int price;
    private List<Integer> numbers;
    private boolean startCard;
    private int sightsRequired;
    private int income;
    private HandlerFunction handlerFunction;
    private AfterBuildFunction afterBuildFunction;
    private String requiredCard;
    private boolean withConfirmation;

    public static Builder create(String name, Type type, int price) {
      Builder builder = new Builder();
      builder.name = name;
      builder.type = type;
      builder.price = price;
      return builder;
    }

    public Builder yellow(List<Integer> numbers) {
      this.color = Color.YELLOW;
      this.numbers = numbers;
      return this;
    }

    public Builder yellow() {
      this.color = Color.YELLOW;
      return this;
    }

    public Builder simpleGreenBlue(Color color, List<Integer> numbers, int income) {
      this.color = color;
      this.income = income;
      this.numbers = numbers;
      this.handlerFunction = ((player, activePlayer, opponents, confirmations, count, notifier) -> {
        add(name, type, player, (income + mall(player)) * count, notifier);
      });
      return this;
    }

    public Builder simpleRed(List<Integer> numbers, int income) {
      this.color = Color.RED;
      this.income = income;
      this.numbers = numbers;
      this.handlerFunction = ((player, activePlayer, opponents, confirmations, count, notifier) -> {
        int minus = remove(name, activePlayer, (income + mall(player)) * count, player, notifier);
        add(name, type, player, minus, notifier);
      });
      return this;
    }

    public Builder complexGreen(List<Integer> numbers, int income, Function<CardHandler, Boolean> checkCard) {
      this.color = Color.GREEN;
      this.income = income;
      this.numbers = numbers;
      this.handlerFunction = ((player, activePlayer, opponents, confirmations, count, notifier) -> {
        final int[] relatedCards = {0};
        player.getCards().forEach((card) -> {
          if (checkCard.apply(card.getHandler())) {
            relatedCards[0] += card.getCount();
          }
        });
        add(name, type, player, (income + mall(player)) * relatedCards[0] * count, notifier);
      });
      return this;
    }

    public Builder forEveryCard(List<Integer> numbers, int income, Function<CardHandler, Boolean> checkCard) {
      this.color = Color.GREEN;
      this.income = income;
      this.numbers = numbers;
      this.handlerFunction = ((player, activePlayer, opponents, confirmations, count, notifier) -> {
        AtomicInteger cupsCount = new AtomicInteger();
        player.getCards().forEach((card) -> {
          if (checkCard.apply(card.getHandler())) {
            cupsCount.addAndGet(card.getCount());
          }
        });
        opponents.forEach(op -> {
          op.getCards().forEach((card) -> {
            if (checkCard.apply(card.getHandler())) {
              cupsCount.addAndGet(card.getCount());
            }
          });
        });
        add(name, type, player, 1 * cupsCount.get() * count, notifier);
      });
      return this;
    }

    public Builder purple(List<Integer> numbers, HandlerFunction handlerFunction) {
      this.numbers = numbers;
      this.color = Color.PURPLE;
      this.handlerFunction = handlerFunction;
      return this;
    }

    public Builder afterBuild(AfterBuildFunction afterBuildFunction) {
      this.afterBuildFunction = afterBuildFunction;
      return this;
    }

    public Builder sightsRequired(int sightsRequired) {
      this.sightsRequired = sightsRequired;
      return this;
    }

    public Builder requiredCard(String requiredCard) {
      this.requiredCard = requiredCard;
      return this;
    }

    public Builder startCard(boolean startCard) {
      this.startCard = startCard;
      return this;
    }

    public Builder withConfirmation(boolean withConfirmation) {
      this.withConfirmation = withConfirmation;
      return this;
    }

    private int mall(GameState.PlayerState player) {
      return player.hasCard("Торговый центр") && (type == CUP || type == BOX) ? 1 : 0;
    }


    public CardHandler build() {
      return new CardHandler() {
        @Override public Color getColor() { return color; }
        @Override public Type getType() { return type; }
        @Override public String getName() { return name; }
        @Override
        public void handle(GameState.PlayerState player, GameState.PlayerState activePlayer, List<GameState.PlayerState> opponents, Map<String, Map<String, Object>> confirmations, int count, Notifier notifier) {
          handlerFunction.handle(player, activePlayer, opponents, confirmations, count, notifier);
        }
        @Override public List<Integer> getNumbers() { return numbers; }
        @Override public int getPrice() { return price;}
        @Override public boolean isStartCard() { return startCard;}
        @Override public int sightsRequired() {return sightsRequired;}
        @Override public boolean isConfirmationRequired(int dice) {
          return withConfirmation && (numbers == null || numbers.contains(dice));
        }
        @Override
        public void doAfterBuild(GameState.PlayerState player) {
          if (afterBuildFunction != null) {afterBuildFunction.handle(player); }
        }
      };
    }

  }

}
