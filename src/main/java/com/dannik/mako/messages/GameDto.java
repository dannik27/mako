package com.dannik.mako.messages;

import com.dannik.mako.model.Game;
import com.dannik.mako.model.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class GameDto {
  private final String id;
  private final String name;
  private final List<Player> players;
  private final String authorName;
  private final String status;

  record Player(String name, boolean isBot) {};

  public static GameDto of(Game game) {
    return new GameDto(game.getId(), game.getName(),
        game.getPlayers().stream().map(user -> new Player(user.getUsername(), user.isBot())).toList(),
        Optional.ofNullable(game.getAuthor()).map(User::getUsername).orElse(null), game.getStatus().name());
  }
}
