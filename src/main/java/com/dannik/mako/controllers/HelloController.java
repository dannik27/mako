package com.dannik.mako.controllers;

import com.dannik.mako.messages.Greeting;
import com.dannik.mako.messages.HelloMessage;
import com.dannik.mako.messages.StartGameRequest;
import com.dannik.mako.services.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HelloController {

  private final GameService gameService;

  @MessageMapping("/hello")
  @SendTo("/topic/greetings")
  public Greeting greeting(@Payload HelloMessage message,
                           @Header("username") String username) throws Exception {
    Thread.sleep(1000); // simulated delay
    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getClientName()) + "!");
  }

  @MessageMapping("/game/start/{username}")
  @SendTo("/topic/resume/{username}")
  public Greeting startGame(@Payload StartGameRequest message,
                            @Header("username") String username,
                            @DestinationVariable("username") String u2) throws Exception {
    gameService.startGame(message.getName(), username);
    return new Greeting("New game is created");
  }

}
