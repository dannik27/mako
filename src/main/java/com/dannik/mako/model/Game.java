package com.dannik.mako.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Game {

  private String id;
  private String name;
  private List<User> players;
  private User author;
  private GameStatus status;

  public Game() {}

  public Game(String name) {
    this.id = name;
    this.name = name;
    this.players = new ArrayList<>();
    this.status = GameStatus.CREATED;
  }

}
