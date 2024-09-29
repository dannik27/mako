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

  class Bakery implements CardHandler {
    @Override
    public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
      add(player, 1 * count);
    }

    @Override
    public List<Integer> getNumbers() {
      return List.of(2, 3);
    }

    @Override
    public int getPrice() {
      return 1;
    }

    @Override
    public Color getColor() {
      return Color.GREEN;
    }

    @Override
    public String getName() {
      return "bakery";
    }
  }

  class Wheat implements CardHandler {

    @Override
    public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
      add(player, 1 * count);
    }

    @Override
    public List<Integer> getNumbers() {
      return List.of(1);
    }

    @Override
    public int getPrice() {
      return 1;
    }

    @Override
    public Color getColor() {
      return Color.BLUE;
    }

    @Override
    public String getName() {
      return "wheat";
    }
  }

  class Forest implements CardHandler {

    @Override
    public void handle(GameState.PlayerState player, List<GameState.PlayerState> opponents, int count) {
      add(player, 1 * count);
    }

    @Override
    public List<Integer> getNumbers() {
      return List.of(5);
    }

    @Override
    public int getPrice() {
      return 3;
    }

    @Override
    public Color getColor() {
      return Color.BLUE;
    }

    @Override
    public String getName() {
      return "forest";
    }
  }
}
