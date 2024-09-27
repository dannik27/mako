package com.dannik.mako.repositories;

import com.dannik.mako.model.Game;
import com.dannik.mako.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class GameRepository {

  private final Map<String, Game> games = new HashMap<>();

  {
    games.put("defaultGame1", new Game("defaultGame1"));
    games.put("defaultGame2", new Game("defaultGame2"));
  }

  public Optional<Game> findActiveGameByUser(User user) {
    return games.values().stream().filter(game -> game.getPlayers().contains(user)).findFirst();
  }

  public Optional<Game> findGameById(String gameId) {
     return Optional.ofNullable(games.get(gameId));
  }

  public Game saveGame(Game game) {
    log.info("New game {} is created", game.getName());
    games.put(game.getId(), game);
    return game;
  }

  public List<Game> findAll() {
    return new ArrayList<>(games.values());
  }

}
