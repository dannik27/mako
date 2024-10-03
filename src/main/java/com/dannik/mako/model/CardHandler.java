package com.dannik.mako.model;

import java.util.List;


public interface CardHandler {

  Color getColor();

  String getName();

  void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count);

  default void add(GameState.PlayerState player, int count) {
    player.setMoney(player.getMoney() + count);
    player.getLastMoneyChange().add(new GameState.MoneyChange(getName(), count, null));
  }

  List<Integer> getNumbers();

  int getPrice();


  enum Color {
    BLUE, GREEN, RED, PURPLE
  }

  static CardHandler simpleBlue(String name, int price, int income, List<Integer> numbers) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.BLUE; }
      @Override public String getName() { return name; }
      @Override
      public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
        add(player, income * count);
      }
      @Override public List<Integer> getNumbers() { return numbers; }
      @Override public int getPrice() { return price;}
    };
  }

  static CardHandler simpleGreen(String name, int price, int income, List<Integer> numbers) {
    return new CardHandler() {
      @Override public Color getColor() { return Color.GREEN; }
      @Override public String getName() { return name; }
      @Override
      public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
        add(player, income * count);
      }
      @Override public List<Integer> getNumbers() { return numbers; }
      @Override public int getPrice() { return price;}
    };
  }
}
