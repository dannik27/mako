package com.dannik.mako.model;

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

  void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, Map<String, Map<String, Object>> confirmations, int count);

  static void add(String cardName, Type cardType, GameState.PlayerState player, int count) {
    int finalCount = mall(player) && (cardType == CUP || cardType == BOX)
        ? count * 2
        : count;
    player.setMoney(player.getMoney() + finalCount);
    player.getLastMoneyChange().add(new GameState.MoneyChange(cardName, finalCount, null));
  }

  static int remove(String cardName, GameState.PlayerState fromWhom, int count, GameState.PlayerState toWhom) {
    if (fromWhom.getMoney() == 0) {
      return 0;
    }
    int minus = Math.min(fromWhom.getMoney(), count);
    fromWhom.setMoney(fromWhom.getMoney() - minus);
    fromWhom.getLastMoneyChange().add(new GameState.MoneyChange(cardName, -minus, toWhom.getUser().getUsername()));
    return minus;
  }

  static boolean mall(GameState.PlayerState player) {
    return player.getCards().containsKey("Торговый центр");
  }

  List<Integer> getNumbers();

  int getPrice();

  boolean isStartCard();

  int sightsRequired();

  boolean isConfirmationRequired(int dice);

  enum Color {
    BLUE, GREEN, RED, PURPLE, YELLOW
  }

  enum Type {
    WHEAT, PIG, GEAR, BOX, ADMIN, FACTORY, CUP, BUSINESS, SHIP, FRUIT
  }

  @FunctionalInterface
  interface HandlerFunction {
    void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, Map<String, Map<String, Object>> confirmations, int count);
  }

  @FunctionalInterface
  interface AfterBuildFunction {
    void handle(GameState.PlayerState player);
  }

  HandlerFunction stadium = (player, opponents, confirmations, count) -> {
    int sum = 0;
    for (GameState.PlayerState opponent : opponents) {
      sum += remove("Стадион", opponent, count * 2, player);
    }
    if (sum > 0) {
      add("Стадион", ADMIN, player, sum);
    }
  };

  HandlerFunction tvCenter = (player, opponents, confirmations, count) -> {
    String targetName = (String) confirmations.get("Телецентр").get("opponent");
    GameState.PlayerState target = opponents.stream().filter(op -> op.getUser().getUsername().equals(targetName)).findFirst()
        .orElseThrow();
    int removed = remove("Телецентр", target, 5, player);
    add("Телецентр", ADMIN, player, removed);
  };

  static HandlerFunction publishing(Map<String, CardHandler> cards) {
    return (player, opponents, confirmations, count) -> {
      int sum = 0;
      for (GameState.PlayerState opponent : opponents) {
        int cardsCount = opponent.getCards().entrySet().stream()
            .filter(e -> cards.get(e.getKey()).getType() == CUP || cards.get(e.getKey()).getType() == BOX)
            .mapToInt(Map.Entry::getValue).sum();
        sum += remove("Издательство", opponent, cardsCount, player);
      }
      if (sum > 0) {
        add("Издательство", ADMIN, player, sum);
      }
    };
  }

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
      this.handlerFunction = ((player, opponents, confirmations, count) -> {
        add(name, type, player, income * count);
      });
      return this;
    }

    public Builder complexGreen(List<Integer> numbers, int income, Map<String, CardHandler> cards, Function<CardHandler, Boolean> checkCard) {
      this.color = Color.GREEN;
      this.income = income;
      this.numbers = numbers;
      this.handlerFunction = ((player, opponents, confirmations, count) -> {
        final int[] relatedCards = {0};
        player.getCards().forEach((card, cardsCount) -> {
          if (checkCard.apply(cards.get(card))) {
            relatedCards[0] += cardsCount;
          }
        });
        add(name, type, player, income * relatedCards[0] * count);
      });
      return this;
    }

    public Builder forEveryCard(List<Integer> numbers, int income, Map<String, CardHandler> cards, Function<CardHandler, Boolean> checkCard) {
      this.color = Color.GREEN;
      this.income = income;
      this.numbers = numbers;
      this.handlerFunction = ((player, opponents, confirmations, count) -> {
        AtomicInteger cupsCount = new AtomicInteger();
        player.getCards().forEach((card, cardsCount) -> {
          if (checkCard.apply(cards.get(card))) {
            cupsCount.addAndGet(cardsCount);
          }
        });
        opponents.forEach(op -> {
          op.getCards().forEach((card, cardsCount) -> {
            if (checkCard.apply(cards.get(card))) {
              cupsCount.addAndGet(cardsCount);
            }
          });
        });
        add(name, type, player, 1 * cupsCount.get() * count);
      });
      return this;
    }

    public Builder purple(List<Integer> numbers, HandlerFunction handlerFunction) {
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


    public CardHandler build() {
      return new CardHandler() {
        @Override public Color getColor() { return color; }
        @Override public Type getType() { return type; }
        @Override public String getName() { return name; }
        @Override
        public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, Map<String, Map<String, Object>> confirmations, int count) {
          handlerFunction.handle(player, opponents, confirmations, count);
        }
        @Override public List<Integer> getNumbers() { return numbers; }
        @Override public int getPrice() { return price;}
        @Override public boolean isStartCard() { return startCard;}
        @Override public int sightsRequired() {return sightsRequired;}
        @Override public boolean isConfirmationRequired(int dice) {
          return withConfirmation && (numbers == null || numbers.contains(dice));
        }
      };
    }

  }

}
