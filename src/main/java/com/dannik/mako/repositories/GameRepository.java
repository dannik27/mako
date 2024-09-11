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


  public Optional<Game> findActiveGameByUser(User user) {
    return games.values().stream().filter(game -> game.getPlayers().contains(user)).findFirst();
  }

  public Game saveGame(Game game) {
    log.info("New game {} is created", game.getName());
    return games.put(game.getId(), game);
  }

}
