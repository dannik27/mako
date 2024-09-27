package com.dannik.mako.messages;

import com.dannik.mako.model.Game;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class ListGamesResponse {

  private final List<GameDto> games;

  public static ListGamesResponse of(List<Game> games) {
    return new ListGamesResponse(games.stream().map(GameDto::of).toList());
  }

  @Data
  @RequiredArgsConstructor
  public static class GameDto {
    private final String id;
    private final String name;
    private final int playersCount;

    public static GameDto of(Game game) {
      return new GameDto(game.getId(), game.getName(), game.getPlayers().size());
    }
  }
}
