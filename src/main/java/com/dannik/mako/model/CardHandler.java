package com.dannik.mako.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public interface CardHandler {

  Color getColor();

  Type getType();

  String getName();

  void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count);

  static void add(String cardName, GameState.PlayerState player, int count) {
    player.setMoney(player.getMoney() + count);
    player.getLastMoneyChange().add(new GameState.MoneyChange(cardName, count, null));
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

  List<Integer> getNumbers();

  int getPrice();


  enum Color {
    BLUE, GREEN, RED, PURPLE, YELLOW
  }

  enum Type {
    WHEAT, PIG, GEAR, BOX, ADMIN, FACTORY
  }

  @FunctionalInterface
  interface HandlerFunction {
    void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count);
  }

  static CardHandler stadium() {
    return purple("Stadium", 6, List.of(6), ((player, opponents, count) -> {
      int sum = 0;
      for (GameState.PlayerState opponent: opponents) {
        sum += remove("Stadium", opponent, count * 2, player);
      }
      if (sum > 0) {
        add("Stadium", player, sum);
      }
    }));
  }

  static CardHandler purple(String name, int price, List<Integer> numbers, HandlerFunction handlerFunction) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.BLUE; }
      @Override public Type getType() { return Type.ADMIN; }
      @Override public String getName() { return name; }
      @Override public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
        handlerFunction.handle(player,opponents,count);
      }
      @Override public List<Integer> getNumbers() { return numbers; }
      @Override public int getPrice() { return price;}
    };
  }

  static CardHandler yellow(String name, int price) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.BLUE; }
      @Override public Type getType() { return Type.ADMIN; }
      @Override public String getName() { return name; }
      @Override public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {}
      @Override public List<Integer> getNumbers() { return Collections.emptyList(); }
      @Override public int getPrice() { return price;}
    };
  }

  static CardHandler simpleBlue(String name, Type type, int price, int income, List<Integer> numbers) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.BLUE; }
      @Override public Type getType() { return type; }
      @Override public String getName() { return name; }
      @Override
      public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
        add(getName(), player, income * count);
      }
      @Override public List<Integer> getNumbers() { return numbers; }
      @Override public int getPrice() { return price;}
    };
  }

  static CardHandler complexGreen(String name, Type relatedType, Map<String, CardHandler> cards, int price, int income, List<Integer> numbers) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.GREEN; }
      @Override public Type getType() { return Type.FACTORY; }
      @Override public String getName() { return name; }
      @Override
      public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
        final int[] relatedCards = {0};
        player.getCards().forEach((card, cardsCount) -> {
          if (cards.get(card).getType() == relatedType) {
            relatedCards[0] += cardsCount;
          }
        });
        add(getName(), player, income * relatedCards[0] * count);
      }
      @Override public List<Integer> getNumbers() { return numbers; }
      @Override public int getPrice() { return price;}
    };
  }

  static CardHandler simpleGreen(String name, Type type, int price, int income, List<Integer> numbers) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.GREEN; }
      @Override public Type getType() { return type; }
      @Override public String getName() { return name; }
      @Override
      public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
        add(getName(), player, income * count);
      }
      @Override public List<Integer> getNumbers() { return numbers; }
      @Override public int getPrice() { return price;}
    };
  }
}
