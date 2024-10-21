package com.dannik.mako.controllers;

import com.dannik.mako.messages.*;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.User;
import com.dannik.mako.services.BotService;
import com.dannik.mako.services.GameService;
import com.dannik.mako.services.GameSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HelloController {

  private final BotService botService;
  private final GameService gameService;
  private final GameSessionService sessionService;

  @MessageMapping("/hello")
  @SendToUser("/topic/greeting")
  public Greeting greeting(@Payload HelloMessage message,
                           @Header("username") String username) throws Exception {

    Optional<Game> activeGame = gameService.getActiveGame(username);
    GameDto activeGameDto = activeGame.map(GameDto::of).orElse(null);

    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getClientName()) + "!", activeGameDto,
            botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
  }

//  @MessageMapping("/game/{gameId}/state")
//  @SendToUser("/topic/game/{gameId}/state")
//  public Greeting greeting(@Payload HelloMessage message,
//                           @Header("username") String username,
//                           @DestinationVariable("gameId") String gameId) throws Exception {
//
//    Optional<Game> activeGame = gameService.getActiveGame(username);
//    GameDto activeGameDto = activeGame.map(GameDto::of).orElse(null);
//
//    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getClientName()) + "!", activeGameDto);
//  }

  @MessageMapping("/game/create")
  @SendToUser("/topic/greeting")
  public Greeting createGame(@Payload StartGameRequest message,
                            @Header("username") String username) throws Exception {
    Game game = gameService.createGame(message.getName(), username);
    String activeGameId = game.getId();
    return new Greeting("New game is created", GameDto.of(game), botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
  }

  @MessageMapping("/game/{gameId}/start")
  @SendToUser("/topic/greeting")
  public Greeting startGame(@Payload ListGamesRequest request,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    Game game = gameService.startGame(gameId, username);

    if (game != null) {
      return new Greeting("You joined the game", GameDto.of(game), botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
    } else {
      return new Greeting("Game is not exist", null, botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
    }
  }

  @MessageMapping("/games/list")
  @SendToUser("/topic/games")
  public ListGamesResponse listGames(@Payload ListGamesRequest request,
                                     @Header("username") String username) throws Exception {
    return ListGamesResponse.of(gameService.getActiveGames());
  }

  @MessageMapping("/game/{gameId}/join")
  @SendToUser("/topic/greeting")
  public Greeting joinGame(@Payload ListGamesRequest request,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    Game game = gameService.joinGame(gameId, username);

    if (game != null) {
      return new Greeting("You joined the game", GameDto.of(game), botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
    } else {
      return new Greeting("Game is not exist", null, botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
    }
  }

  record AddBotRequest(String botName) {};

  @MessageMapping("/game/{gameId}/add-bot")
  public void addBot(@Payload AddBotRequest request,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    gameService.addBot(gameId, username, request.botName());
  }

  @MessageMapping("/game/{gameId}/remove-bot")
  public void removeBot(@Payload AddBotRequest request,
                     @Header("username") String username,
                     @DestinationVariable("gameId") String gameId) throws Exception {

    gameService.removeBot(gameId, username, request.botName());
  }

  @MessageMapping("/game/{gameId}/leave")
  @SendToUser("/topic/greeting")
  public Greeting leaveGame(@Payload ListGamesRequest request,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    Game game = gameService.leaveGame(gameId, username);

    if (game != null) {
      return new Greeting("You leaved the game", null, botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
    } else {
      return new Greeting("Game is not exist", null, botService.getBots().keySet().stream().map(Greeting.BotDto::new).toList());
    }
  }

}
