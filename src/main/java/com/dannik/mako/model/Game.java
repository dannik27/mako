package com.dannik.mako.model;

import lombok.Data;

import java.util.List;

@Data
public class Game {

  private String id;
  private String name;
  private List<User> players;

}
