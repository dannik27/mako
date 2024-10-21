package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.model.Game;
import com.dannik.mako.model.GameState;
import com.dannik.mako.model.User;
import com.dannik.mako.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.Map;

import static com.dannik.mako.services.BotHandler.*;
import static org.mockito.ArgumentMatchers.any;

public class GameSessionServiceTest {

//    @Test
    public void tee() {

        GameService gameService = Mockito.mock(GameService.class);
        UserNotifier notifier = Mockito.mock(UserNotifier.class);

        BotHandler bot = new BotHandler.Builder()
                .when(dicePhase(), hasCard("Вокзал"))
                .thenDo(diceRoll(2))
                .when(dicePhase())
                .thenDo(diceRoll(1))

                .when(buildPhase(), always())
                .thenDo(buyCard("Аэропорт", "Радиовышка", "Парк развлечений", "Торговый центр"))
                .when(buildPhase(), turnAfter(8))
                .thenDo(buyCard("Вокзал"))
                .when(buildPhase(), hasCard("Аэропорт"))
                .thenDo(buyCard("Порт"))
                .when(buildPhase(), not(hasCard("Вокзал")))
                .thenDo(buyCard("Кукурузное поле", "Ферма"))
                .when(buildPhase(), hasCard("Вокзал"))
                .thenDo(buyCard("Сыроварня", "Налоговая", "Ферма", "Пиццерия", "Закусочная"))
                .when(buildPhase())
                .thenDo(skipBuild())

                .when(choicePhase(), choiceTarget("Порт"))
                .thenDo(doChoice(Map.of("addition", 0)))
                .when(choicePhase(), choiceTarget("Радиовышка"), hasCard("Вокзал"), not(diceIn(2, 7)))
                .thenDo(doChoice(Map.of("diceCount", 2)))
                .when(choicePhase(), choiceTarget("Радиовышка"), not(hasCard("Вокзал")), not(diceIn(2, 3)))
                .thenDo(doChoice(Map.of("diceCount", 1)))
                .when(choicePhase(), choiceTarget("Радиовышка"))
                .thenDo(doChoice(Map.of("diceCount", 0)))

                .build();

        BotHandler bot2 = new BotHandler.Builder()
                .when(dicePhase())
                .thenDo(diceRoll(1))
                .when(buildPhase())
                .thenDo(skipBuild())
                .build();

        GameSessionService gameSessionService = new GameSessionService(notifier, gameService);

        Mockito.doAnswer(e -> {
            String message = e.getArgument(1, String.class);
            System.out.println("Event: " + message);
            return null;
        }).when(notifier).notifyEvent(any(), any());

        Mockito.doAnswer(e -> {
            GameState state = e.getArgument(0, GameState.class);
            String username = state.getActivePlayer().getUser().getUsername();
            if (username.equals("bot1")) {
                bot.handle(GameStateResponse.of(state), gameSessionService, username, state.getGame().getId());
            } else {
                bot2.handle(GameStateResponse.of(state), gameSessionService, username, state.getGame().getId());
            }
            return null;
        }).when(notifier).notifyGameState(any());

        Game game = new Game("test-game");
        game.getPlayers().add(new User("bot1", true, true));
        game.getPlayers().add(new User("bot2", true, true));

        gameSessionService.init(game);

    }
}
