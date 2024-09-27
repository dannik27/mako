package com.dannik.mako.controllers;

import com.dannik.mako.messages.*;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.User;
import com.dannik.mako.services.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HelloController {

  private final GameService gameService;

  @MessageMapping("/hello")
  @SendToUser("/topic/greeting")
  public Greeting greeting(@Payload HelloMessage message,
                           @Header("username") String username) throws Exception {

    Optional<Game> activeGame = gameService.getActiveGame(username);
    String activeGameId = activeGame.map(Game::getId).orElse(null);

    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getClientName()) + "!", activeGameId);
  }

  @MessageMapping("/game/{gameId}/state")
  @SendToUser("/topic/game/{gameId}/state")
  public Greeting greeting(@Payload HelloMessage message,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    Optional<Game> activeGame = gameService.getActiveGame(username);
    String activeGameId = activeGame.map(Game::getId).orElse(null);

    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getClientName()) + "!", activeGameId);
  }

  @MessageMapping("/game/start")
  @SendToUser("/topic/greeting")
  public Greeting startGame(@Payload StartGameRequest message,
                            @Header("username") String username) throws Exception {
    Game game = gameService.startGame(message.getName(), username);
    String activeGameId = game.getId();
    return new Greeting("New game is created", activeGameId);
  }

  @MessageMapping("/games/list")
  @SendToUser("/topic/games")
  public ListGamesResponse listGames(@Payload ListGamesRequest request,
                                     @Header("username") String username) throws Exception {
    return ListGamesResponse.of(gameService.getGames());
  }

  @MessageMapping("/game/{gameId}/join")
  @SendToUser("/topic/greeting")
  public Greeting joinGame(@Payload ListGamesRequest request,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    Game game = gameService.joinGame(gameId, username);

    if (game != null) {
      return new Greeting("You joined the game", gameId);
    } else {
      return new Greeting("Game is not exist", null);
    }
  }

  @MessageMapping("/game/{gameId}/leave")
  @SendToUser("/topic/greeting")
  public Greeting leaveGame(@Payload ListGamesRequest request,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    Game game = gameService.leaveGame(gameId, username);

    if (game != null) {
      return new Greeting("You leaved the game", null);
    } else {
      return new Greeting("Game is not exist", null);
    }
  }

}
