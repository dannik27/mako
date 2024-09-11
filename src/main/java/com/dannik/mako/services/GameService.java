package com.dannik.mako.services;

import com.dannik.mako.model.Game;
import com.dannik.mako.model.User;
import com.dannik.mako.repositories.GameRepository;
import com.dannik.mako.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameService {

  private final GameRepository gameRepository;
  private final UserRepository userRepository;

  public Game startGame(String name, String username) {
    User user = userRepository.getOrCreate(username);

    Game newGame = new Game();
    newGame.setId(UUID.randomUUID().toString());
    List<User> players = new ArrayList<>();
    players.add(user);
    newGame.setPlayers(players);
    newGame.setName(name);
    return gameRepository.saveGame(newGame);
  }

}
