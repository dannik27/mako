package com.dannik.mako.controllers;

import com.dannik.mako.messages.*;
import com.dannik.mako.model.Game;
import com.dannik.mako.services.GameService;
import com.dannik.mako.services.GameSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class GameSessionController {

  private final GameSessionService sessionService;


  @MessageMapping("/session/{gameId}/state")
  public void requestState(@Payload HelloMessage message,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.requestState(gameId);
  }

  record DiceRoll(int count) {}

  @MessageMapping("/session/{gameId}/dice")
  public void diceRoll(@Payload DiceRoll message,
                           @Header("username") String username,
                           @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.diceRoll(gameId, username, message.count());
  }

  @MessageMapping("/session/{gameId}/buy")
  public void buyCard(@Payload BuyCardMessage message,
                       @Header("username") String username,
                       @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.buyCard(gameId, username, message.getName());
  }

  @MessageMapping("/session/{gameId}/skip-build")
  public void skipBuild(@Payload BuyCardMessage message,
                      @Header("username") String username,
                      @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.skipBuild(gameId, username);
  }

  @MessageMapping("/session/{gameId}/fund")
  public void addToFund(@Payload BuyCardMessage message,
                        @Header("username") String username,
                        @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.addToFund(gameId, username);
  }



  @MessageMapping("/session/{gameId}/confirm")
  public void confirm(@Payload Map<String, Object> message,
                        @Header("username") String username,
                        @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.confirm(gameId, username, message);
  }

  @MessageMapping("/session/{gameId}/leave")
  public void leave(@Payload Map<String, Object> message,
                      @Header("username") String username,
                      @DestinationVariable("gameId") String gameId) throws Exception {

    sessionService.leaveGame(gameId, username);
  }


}
