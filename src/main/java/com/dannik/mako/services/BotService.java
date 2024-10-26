package com.dannik.mako.services;

import com.dannik.mako.messages.GameStateResponse;
import com.dannik.mako.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.dannik.mako.services.BotHandler.*;

@Component
@RequiredArgsConstructor
public class BotService {

  private final UserRepository userRepository;

  @Autowired
  @Setter
  @Lazy
  private GameSessionService sessionService;

  @Getter
  private final Map<String, BotHandler> bots = new HashMap<>();

  @PostConstruct
  public void init() {
    bots.put("Петька", mrBlue);
    bots.put("Матрёна", mrBox);
    bots.put("Сильвестр", cheeseMan);
    bots.put("Семён", gearMan);
    userRepository.create("Петька", true);
    userRepository.create("Матрёна", true);
    userRepository.create("Сильвестр", true);
    userRepository.create("Семён", true);
  }

    public void notifyBots(GameStateResponse gameState, String gameId) {
    String activePlayerName = gameState.getActivePlayer();
    BotHandler handler = bots.get(activePlayerName);
    if (handler == null) {
      return;
    }
    handler.handle(gameState, sessionService, activePlayerName, gameId);
  }

  public BotHandler mrBlue = new BotHandler.Builder()
          .when(dicePhase(), hasCard("Вокзал"))
          .thenDo(diceRoll(2))
          .when(dicePhase())
          .thenDo(diceRoll(1))

          .when(buildPhase(), always())
          .thenDo(buyCard("Аэропорт", "Радиовышка", "Парк развлечений"))
          .when(buildPhase(), turnAfter(6))
          .thenDo(buyCard("Вокзал"))
          .when(buildPhase(), hasCard("Аэропорт"))
          .thenDo(buyCard("Торговый центр", "Порт"))
          .when(buildPhase(), hasCard("Аэропорт")) // If bot has airport and can not buy any yellow card - skip
          .thenDo(skipBuild())
          .when(buildPhase(), not(hasCard("Вокзал")))
          .thenDo(buyCard("Лес", "Кукурузное поле", "Цветник", "Ферма", "Пшеничное поле"))
          .when(buildPhase(), hasCard("Вокзал"))
          .thenDo(buyCard("Шахта", "Виноградник", "Яблоневый сад"))
          .when(buildPhase())
          .thenDo(skipBuild())

          .when(choicePhase(), choiceTarget("Порт"))
          .thenDo(doChoice(Map.of("addition", 0)))
          .when(choicePhase(), choiceTarget("Радиовышка"), hasCard("Вокзал"), not(diceIn(5, 7, 9)))
          .thenDo(doChoice(Map.of("diceCount", 2)))
          .when(choicePhase(), choiceTarget("Радиовышка"), not(hasCard("Вокзал")), not(diceIn(2, 4, 5)))
          .thenDo(doChoice(Map.of("diceCount", 1)))
          .when(choicePhase(), choiceTarget("Радиовышка"))
          .thenDo(doChoice(Map.of("diceCount", 0)))

          .build();

  public BotHandler mrBox = new BotHandler.Builder()
          .when(dicePhase())
          .thenDo(diceRoll(1))

          .when(buildPhase(), always())
          .thenDo(buyCard("Торговый центр", "Аэропорт", "Радиовышка", "Парк развлечений"))
          .when(buildPhase(), hasCard("Аэропорт"))
          .thenDo(buyCard("Порт", "Вокзал"))
          .when(buildPhase(), hasCard("Цветник", 3))
          .thenDo(buyCard("Цветочный магазин"))
          .when(buildPhase())
          .thenDo(buyCard("Магазин", "Цветник", "Пекарня", "Универсам"))
          .when(buildPhase())
          .thenDo(skipBuild())

          .when(choicePhase(), choiceTarget("Порт"))
          .thenDo(doChoice(Map.of("addition", 0)))
          .when(choicePhase(), choiceTarget("Радиовышка"), diceIn(1, 5))
          .thenDo(doChoice(Map.of("diceCount", 1)))
          .when(choicePhase(), choiceTarget("Радиовышка"))
          .thenDo(doChoice(Map.of("diceCount", 0)))

          .build();

  public BotHandler cheeseMan = new BotHandler.Builder()
          .when(dicePhase(), hasCard("Вокзал"))
          .thenDo(diceRoll(2))
          .when(dicePhase())
          .thenDo(diceRoll(1))

          .when(buildPhase(), always())
          .thenDo(buyCard("Аэропорт", "Радиовышка"))
          .when(buildPhase(), turnAfter(8))
          .thenDo(buyCard("Вокзал"))
          .when(buildPhase(), hasCard("Аэропорт"))
          .thenDo(buyCard("Торговый центр", "Порт", "Парк развлечений"))
          .when(buildPhase(), not(hasCard("Вокзал")))
          .thenDo(buyCard("Кукурузное поле", "Ферма"))
          .when(buildPhase(), hasCard("Вокзал"))
          .thenDo(buyCard("Сыроварня", "Налоговая", "Ферма", "Стадион", "Пиццерия", "Закусочная"))
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

  public BotHandler gearMan = new BotHandler.Builder()
          .when(dicePhase(), hasCard("Вокзал"))
          .thenDo(diceRoll(2))
          .when(dicePhase())
          .thenDo(diceRoll(1))

          .when(buildPhase(), always())
          .thenDo(buyCard("Аэропорт", "Радиовышка"))
          .when(buildPhase(), turnAfter(8))
          .thenDo(buyCard("Вокзал"))
          .when(buildPhase(), hasCard("Аэропорт"))
          .thenDo(buyCard("Парк развлечений", "Торговый центр", "Порт"))
          .when(buildPhase(), not(hasCard("Вокзал")))
          .thenDo(buyCard("Лес", "Ферма"))
          .when(buildPhase(), hasCard("Вокзал"))
          .thenDo(buyCard("Шахта", "Сыроварня", "Мебельная фабрика", "Налоговая", "Стадион", "Лес", "Ферма", "Пиццерия", "Закусочная"))
          .when(buildPhase())
          .thenDo(skipBuild())

          .when(choicePhase(), choiceTarget("Порт"))
          .thenDo(doChoice(Map.of("addition", 0)))
          .when(choicePhase(), choiceTarget("Радиовышка"), hasCard("Вокзал"), not(diceIn(5, 7, 9)))
          .thenDo(doChoice(Map.of("diceCount", 2)))
          .when(choicePhase(), choiceTarget("Радиовышка"), not(hasCard("Вокзал")), not(diceIn(2, 3, 5, 6)))
          .thenDo(doChoice(Map.of("diceCount", 1)))
          .when(choicePhase(), choiceTarget("Радиовышка"))
          .thenDo(doChoice(Map.of("diceCount", 0)))

          .build();
}
