package com.dannik.mako.messages;

import com.dannik.mako.model.GameState;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GameStateResponse {

  private final String activePlayer;
  private final boolean twoDices;
  private final List<PlayerStateDto> players;
  private final String phase;
  private final int lastDice;
  private final String lastBuilding;
  private final String winner;
  private final int turn;
  private final String requiredConfirmation;

  public static GameStateResponse of(GameState state) {
    return of(state, null);
  }

  public static GameStateResponse of(GameState state, String winner) {
    boolean twoDices = state.getPlayers().get(state.getActivePlayerIndex()).hasCard("Вокзал");

    return new GameStateResponse(state.getPlayers().get(state.getActivePlayerIndex()).getUser().getUsername(),
        twoDices,
        state.getPlayers().stream().map(PlayerStateDto::of).toList(),
        state.getPhase().name(),
        state.getLastDice(),
        state.getLastBuilding(), winner, state.getTurn(), state.getRequiredConfirmation());
  }

  @Data
  @RequiredArgsConstructor
  public static class PlayerStateDto {
    private final String name;
    private final boolean online;
    private final boolean bot;
    private final int money;
    private final Map<String, Integer> cards;
    private final Map<String, Integer> disabledCards;
    private final List<GameState.MoneyChange> lastMoneyChange;
    private final List<GameState.MoneyChange> moneyChangeHistory;
    private final String lastBoughtCard;
    private final boolean left;

    private final boolean fundAvailable;
    private final int fundValue;

    public static PlayerStateDto of(GameState.PlayerState state) {
      return new PlayerStateDto(state.getUser().getUsername(), state.getUser().isOnline(), state.getUser().isBot(),
          state.getMoney(), state.getCards().stream().collect(Collectors.toMap(c -> c.getHandler().getName(), GameState.CardState::getCount)),
          state.getCards().stream().collect(Collectors.toMap(c -> c.getHandler().getName(), GameState.CardState::getDisabledCount)),
          state.getLastMoneyChange(), state.getMoneyChangeHistory(), state.getLastBoughtCard(), state.isLeft(),
              state.hasCard("Венчурный фонд") && !state.isFundUsed(),
              state.getFundValue());
    }
  }

//  @Data
//  @RequiredArgsConstructor
//  public static class CardStateDto {
//    private final String name;
//    private final String color;
//  }

}
