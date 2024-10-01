package com.dannik.mako.services;

import com.dannik.mako.controllers.GameSessionController;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameStatus;
import com.dannik.mako.model.User;
import com.dannik.mako.repositories.GameRepository;
import com.dannik.mako.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameService {

  private final GameRepository gameRepository;
  private final UserRepository userRepository;
  private final GameSessionService sessionService;
  private final UserNotifier notifier;

  public Game createGame(String name, String username) {
    User user = userRepository.getOrCreate(username);

    Game newGame = new Game();
    newGame.setId(UUID.randomUUID().toString());
    List<User> players = new ArrayList<>();
    players.add(user);
    newGame.setPlayers(players);
    newGame.setName(name);
    newGame.setAuthor(user);
    newGame.setStatus(GameStatus.CREATED);
    gameRepository.saveGame(newGame);
    notifier.notifyGameUpdate(newGame);
    return newGame;
  }

  public Game joinGame(String gameId, String username) {
    User user = userRepository.getOrCreate(username);

    return gameRepository.findGameById(gameId).map(game -> {
      game.getPlayers().add(user);
      notifier.notifyGameUpdate(game);
      return game;
    }).orElse(null);
  }

  public void addBot(String gameId, String username, String botName) {
    User bot = userRepository.getByName(botName)
        .orElseThrow(() -> new RuntimeException("Bot with name " + botName + " not found"));

    gameRepository.findGameById(gameId).ifPresentOrElse(game -> {
      game.getPlayers().add(bot);
      notifier.notifyGameUpdate(game);
    }, () -> log.error("Game not found: " + gameId));
  }

  public void removeBot(String gameId, String username, String botName) {
    User bot = userRepository.getByName(botName)
        .orElseThrow(() -> new RuntimeException("Bot with name " + botName + " not found"));

    gameRepository.findGameById(gameId).ifPresentOrElse(game -> {
      game.getPlayers().remove(bot);
      notifier.notifyGameUpdate(game);
    }, () -> log.error("Game not found: " + gameId));
  }

  public Game startGame(String gameId, String username) {
    User user = userRepository.getOrCreate(username);

    return gameRepository.findGameById(gameId).map(game -> {
      game.setStatus(GameStatus.IN_PROGRESS);
      sessionService.init(game);
      notifier.notifyGameUpdate(game);
      return game;
    }).orElse(null);
  }

  public Game leaveGame(String gameId, String username) {
    User user = userRepository.getOrCreate(username);

    return gameRepository.findGameById(gameId).map(game -> {

      if (game.getStatus() == GameStatus.CREATED && user.equals(game.getAuthor())) {
        gameRepository.deleteGame(gameId);
        notifier.notifyGameDelete(game);
      } else {
        game.getPlayers().remove(user);
        notifier.notifyGameUpdate(game);
      }
      return game;
    }).orElse(null);
  }

  public List<Game> getGames() {
    return gameRepository.findAll();
  }

  public Optional<Game> getGame(String gameId) {
    return gameRepository.findGameById(gameId);
  }

  public Optional<Game> getActiveGame(String username) {
    User user = userRepository.getOrCreate(username);

    return gameRepository.findActiveGameByUser(user);
  }

}
