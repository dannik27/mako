package com.dannik.mako.messages;

import com.dannik.mako.model.Game;
import com.dannik.mako.model.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
public class ListGamesResponse {

  private final List<GameDto> games;

  public static ListGamesResponse of(List<Game> games) {
    return new ListGamesResponse(games.stream().map(GameDto::of).toList());
  }
}
