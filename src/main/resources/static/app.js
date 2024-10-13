


const { createApp, ref, onMounted, computed } = Vue

const stompClient = new StompJs.Client({
//    brokerURL: 'ws://localhost:8080/qwe?username='
    brokerURL: '/qwe?username='
});


createApp({
  setup() {

    const LOGIN_SCREEN = 'login'
    const MENU_SCREEN = 'main-menu'
    const LOBBY_SCREEN = 'lobby'
    const GAME_SCREEN = 'game'

    const username = ref('')
    const newGameName = ref('')

    const connected = ref(false)
    const mainScreen = ref('login')
    const showMainMenu = computed(() => mainScreen.value == MENU_SCREEN)
    const showLobbyScreen = computed(() => mainScreen.value == LOBBY_SCREEN)
    const showGameScreen = computed(() => mainScreen.value == GAME_SCREEN)
    const games = ref([])
    const currentGame = ref(null)

    const playerState = ref({money: 0})
    const opponents = ref({})
    const activePlayer = ref('')
    const gamePhase = ref('')
    const winner = ref('')
    const lastDice = ref('')
    const twoDices = ref(false)
    const requiredConfirmation = ref('')
    const playerInfoModal = ref(false)
    const historyModal = ref(false)
    const events = ref([])

    const cards = ref('')

    const onCardsReceived = (message) => {
      console.log("Cards received: " + message['body'])
      cards.value = JSON.parse(message['body'])
    }

    const onGameStateReceived = (message) => {
        console.log("Game state received: " + message['body'])
        let newState = JSON.parse(message['body'])

        let currentPlayerIndex = newState['players'].findIndex(player => player.name == username.value)

        playerState.value = newState['players'][currentPlayerIndex]
        opponents.value = newState['players'].toSpliced(currentPlayerIndex, 1)

        activePlayer.value = newState.activePlayer
        gamePhase.value = newState.phase
        lastDice.value = newState.lastDice
        twoDices.value = newState.twoDices
        winner.value = newState.winner
        requiredConfirmation.value = newState.requiredConfirmation

        if (playerState.value.left) {
          reset()
        }

//        .filter(player => player.name != username.value)
    }

    const reset = () => {
      stompClient.publish({
          destination: "/app/hello",
          body: JSON.stringify({'clientName': username.value}),
          headers: { username: username.value }
      });
    }

    const onEventReceived = (message) => {
      console.log("Event: " + message['body'])
      events.value.push(message['body'])
    }

    let subscriptions = []

    const subscribeGame = (gameId) => {
        subscriptions.push(stompClient.subscribe(`/topic/game/${gameId}/state`, onGameStateReceived));
        subscriptions.push(stompClient.subscribe(`/topic/game/${gameId}/cards`, onCardsReceived));
        subscriptions.push(stompClient.subscribe(`/topic/game/${gameId}/event`, onEventReceived));
    }

    const unsubscribeGame = () => {
      subscriptions.forEach(s => s.unsubscribe())
      subscriptions = []
    }

    const onWsConnect = (frame) => {
      console.log('Connected: ' + frame);

      stompClient.subscribe('/user/topic/greeting', (message) => {
          let body = JSON.parse(message['body'])
          console.log(body)
          if (body.activeGame) {
            currentGame.value = body.activeGame
            console.log("Game already started");
            subscribeGame(currentGame.value.id)

            if (currentGame.value.status == 'CREATED') {
                mainScreen.value = LOBBY_SCREEN
            } else {
                mainScreen.value = GAME_SCREEN

                stompClient.publish({
                    destination: `/app/session/${currentGame.value.id}/state`,
                    body: "{}",
                    headers: { username: username.value }
                });
            }
          } else {
            unsubscribeGame()
            currentGame.value = null
            mainScreen.value = MENU_SCREEN
            stompClient.publish({
                destination: "/app/games/list",
                body: "{}",
                headers: { username: username.value }
            });

          }
//          showGreeting(body.message);
      });

      stompClient.subscribe('/user/topic/games', (message) => {
          console.log("Games received: " + message['body'])
          games.value = JSON.parse(message['body'])['games']
      });

      stompClient.subscribe('/topic/games/update', (message) => {
          console.log("Game update received: " + message['body'])
          for (let [key, value] of Object.entries(JSON.parse(message['body'])['games'])) {

              if (currentGame.value && currentGame.value.id == key) {
                  currentGame.value = value
                  if (currentGame.value.status == 'CREATED') {
                      mainScreen.value = LOBBY_SCREEN
                  } else {
                      mainScreen.value = GAME_SCREEN
                  }
              }

              let index = games.value.findIndex(game => game.id == key)

              if (index > -1 && value == null) {
                games.value.splice(index, 1)
              } else if (index > -1) {
                games.value[index] = value
              } else {
                games.value.push(value)
              }
          }
      });

      reset()

      connected.value = true
    }

    const join = (gameId) => {
      stompClient.publish({
          destination: `/app/game/${gameId}/join`,
          body: "{}",
          headers: { username: username.value }
      });
    }

    const leave = () => {
      stompClient.publish({
          destination: `/app/game/${currentGame.value.id}/leave`,
          body: "{}",
          headers: { username: username.value }
      });
    }

    const createGame = () => {
      stompClient.publish({
          destination: "/app/game/create",
          body: JSON.stringify({'name': newGameName.value}),
          headers: { username: username.value }
      });
    }

    const startGame = () => {
      stompClient.publish({
          destination: `/app/game/${currentGame.value.id}/start`,
          body: "{}",
          headers: { username: username.value }
      });
    }

    const addBot = (botName) => {
      stompClient.publish({
          destination: `/app/game/${currentGame.value.id}/add-bot`,
          body: JSON.stringify({'botName': botName}),
          headers: { username: username.value }
      });
    }

    const removeBot = (botName) => {
      stompClient.publish({
          destination: `/app/game/${currentGame.value.id}/remove-bot`,
          body: JSON.stringify({'botName': botName}),
          headers: { username: username.value }
      });
    }

    const connect = () => {
        if (username.value == null || username.value.length == 0) {
          console.log("Username must not be empty")
          return
        }
        console.log("qqq " + username.value)
        localStorage.setItem("username", username.value)
        stompClient.onConnect = onWsConnect
        stompClient.activate();
    }

    onMounted(() => {
      let savedUser = localStorage.getItem("username")
      if (savedUser) {
        username.value = savedUser
        connect()
      }
    })


    const disconnect = () => {
        stompClient.deactivate();
        connected.value = false
        mainScreen.value = LOGIN_SCREEN
        localStorage.removeItem("username")
    }


    const diceRoll = (count) => {
        stompClient.publish({
            destination: `/app/session/${currentGame.value.id}/dice`,
            body: JSON.stringify({ count: count}),
            headers: { username: username.value }
        });
    }

    const buyCard = (cardName) => {
        if (canBuyCard(cardName)) {
          stompClient.publish({
              destination: `/app/session/${currentGame.value.id}/buy`,
              body: JSON.stringify({ name: cardName}),
              headers: { username: username.value }
          });
        }
    }

    const skipBuild = () => {
        stompClient.publish({
            destination: `/app/session/${currentGame.value.id}/skip-build`,
            body: "{}",
            headers: { username: username.value }
        });
    }

    const calculateLastChange = (cardName) => {
      let change = playerState.value.lastMoneyChange.find(c => c.card == cardName)
      return change?.count
    }

    const isLastBoughtCard = (playerName, cardName) => {
      let player = opponents.value.find(o => o.name == playerName)
      return cardName == player?.lastBoughtCard
    }

    const cardsLeftCount = (cardName) => {
      let card = cards.value.find(c => c.name == cardName)
      if (card.color == "YELLOW" || card.color == "PURPLE") {
        return playerState.value.cards.hasOwnProperty(cardName) ? 0 : 1
      } else {

        let occupied = 0

        opponents.value.forEach(op => {
          let occupiedByOpponent = op.cards[cardName]
          if (occupiedByOpponent) {
            occupied += occupiedByOpponent
          }
        })

        let occupiedByPlayer = playerState.value.cards[cardName]
        if (occupiedByPlayer) {
          occupied += occupiedByPlayer
        }

        if (card.startCard) {
          occupied -= 1
          occupied -= opponents.value.length
        }

        return 6 - occupied
      }

    }

    const canBuyCard = (cardName) => {
      let card = cards.value.find(c => c.name == cardName)
      return card.price <= playerState.value.money && cardsLeftCount(cardName) > 0
    }

    const cardsByColor = (color) => {
      return cards.value.filter(c => c.color == color && cardsLeftCount(c.name) > 0).toSorted((a, b) => {
        let valueA = a.numbers ? a.numbers[0] : a.price
        let valueB = b.numbers ? b.numbers[0] : b.price
        return valueA - valueB
        }).map(c => ({
          name: c.name,
          price: c.price,
          cardsLeft: cardsLeftCount(c.name),
          numbers: c.numbers == null ? '' : c.numbers.length == 1 ? c.numbers[0] : c.numbers[0] + '-' + c.numbers[c.numbers.length - 1],
          canBuy: canBuyCard(c.name)
        }))

    }

    const confirm = (cardName, body) => {
      stompClient.publish({
          destination: `/app/session/${currentGame.value.id}/confirm`,
          body: JSON.stringify({ name: cardName, ...body}),
          headers: { username: username.value }
      });
    }

    const shopGroups = ref([
      {
        color: 'GREEN',
        activeStyle: { 'background-color': '#90EE90' },
        disabledStyle: { 'background-color': '#8FBC8F' }
      },
      {
        color: 'BLUE',
        activeStyle: { 'background-color': '#00BFFF' },
        disabledStyle: { 'background-color': '#4682B4' }
      },
      {
        color: 'RED',
        activeStyle: { 'background-color': '#FF6347' },
        disabledStyle: { 'background-color': '#800000' }
      },
      {
        color: 'PURPLE',
        activeStyle: { 'background-color': '#BA55D3' },
        disabledStyle: { 'background-color': '#9370DB' }
      },
      {
        color: 'YELLOW',
        activeStyle: { 'background-color': '#EEE8AA' },
        disabledStyle: { 'background-color': '#778899' }
      }
    ])

    const boughtCards = (user) => {
      let cardsState = user == username.value
            ? playerState.value.cards
            : opponents.value.find(op => op.name == user).cards

      return Object.entries(cardsState)
        .map(e => {
        let card = cards.value.find(c => c.name == e[0])
        let shopGroup = shopGroups.value.find(g => g.color == card.color)
          return {
            name: e[0],
            count: e[1],
            color: card.color,
            numbers: card.numbers,
            numbersStr: card.numbers == null ? '' : card.numbers.length == 1 ? card.numbers[0] : card.numbers[0] + '-' + card.numbers[card.numbers.length - 1],
            style: shopGroup.activeStyle
            }
        }).filter(c => c.color != 'YELLOW')
          .toSorted((a, b) => {
             if (a.color == b.color) {
                return a.numbers[0] - b.numbers[0]
             } else {
                return shopGroups.value.findIndex(g => g.color == a.color) - shopGroups.value.findIndex(g => g.color == b.color)
             }
           })
    }

    const yellowCards = (user) => {
      let cardHandlers = cards.value.filter(c => c.color == 'YELLOW')
        .toSorted((a, b) => {
          let valueA = a.numbers ? a.numbers[0] : a.price
          let valueB = b.numbers ? b.numbers[0] : b.price
          return valueA - valueB
          })

      let cardsState = user == username.value
        ? playerState.value.cards
        : opponents.value.find(op => op.name == user).cards

      return cardHandlers.map(c => {

        return {
          name: c.name,
          price: c.price,
          style: cardsState.hasOwnProperty(c.name) ? { 'background-color': '#EEE8AA' } : { 'background-color': '#778899' }
        }

      })

    }

    const leaveGame = () => {
      stompClient.publish({
          destination: `/app/session/${currentGame.value.id}/leave`,
          body: "{}",
          headers: { username: username.value }
      });
    }


    const message = ref('Hello vue!')
    return {
      connected,
      username,
      connect,
      disconnect,
      showMainMenu,
      showLobbyScreen,
      showGameScreen,
      message,
      reset,
      mainScreen,

      games,
      join,
      leave,

      newGameName,
      createGame,
      currentGame,
      startGame,
      addBot,
      removeBot,

      playerState,
      opponents,
      activePlayer,
      gamePhase,
      cards,
      lastDice,
      twoDices,
      calculateLastChange,
      isLastBoughtCard,
      canBuyCard,
      cardsLeftCount,
      cardsByColor,
      winner,
      requiredConfirmation,
      confirm,
      shopGroups,
      boughtCards,
      yellowCards,
      playerInfoModal,
      historyModal,
      events,
      leaveGame,

      diceRoll,
      buyCard,
      skipBuild
    }
  }
}).mount('#app')





let userName = ""
let activeGameId = ""



//stompClient.onConnect = (frame) => {
//    setConnected(true);
//    console.log('Connected: ' + frame);
//    stompClient.subscribe('/user/topic/greeting', (message) => {
//        let body = JSON.parse(message['body'])
//        console.log(body)
//        if (body.activeGame) {
//          console.log("Game already started");
//          showGame(body.activeGame);
//        } else {
//          showMainMenu();
//        }
//        showGreeting(body.message);
//    });
//    stompClient.subscribe('/topic/resume/Danya', (greeting) => {
//        showGreeting("Game started");
//        showGame();
//    });
//    stompClient.subscribe('/topic/games/Danya', (message) => {
//        let games = JSON.parse(message['body'])['games']
//        showGames(games);
//    });
//
//    stompClient.publish({
//        destination: "/app/hello",
//        body: JSON.stringify({'clientName': $("#name").val()}),
//        headers: { username: userName }
//    });
//
//};

//stompClient.onWebSocketError = (error) => {
//    console.error('Error with websocket', error);
//};
//
//stompClient.onStompError = (frame) => {
//    console.error('Broker reported error: ' + frame.headers['message']);
//    console.error('Additional details: ' + frame.body);
//};
//
//function showGame(gameId) {
//  activeGameId = gameId
//  $("#main-content").hide();
//  $("#game-content").show();
//
//  stompClient.publish({
//      destination: "/app/game/{activeGameId}/state",
//      headers: { username: userName }
//  });
//
//  stompClient.subscribe('/topic/game/' + activeGameId + "/state", (message) => {
//      let body = JSON.parse(message['body'])
//      if (body.status === 'lobby') {
//          console.log("lobby")
//      }
//      if (body.status === 'turn') {
//          console.log("turn")
//      }
//  });
//
//}
//
//function setConnected(connected) {
//    $("#connect").prop("disabled", connected);
//    $("#disconnect").prop("disabled", !connected);
//    if (connected) {
//        $("#conversation").show();
//    }
//    else {
//        $("#conversation").hide();
//    }
//    $("#greetings").html("");
//}

//function connect() {
//    userName = $("#name").val()
//    if (userName == null || userName.length == 0) {
//      console.log("Username must not be empty")
//      return
//    }
//    stompClient.activate();
//}

//function disconnect() {
//    stompClient.deactivate();
//    setConnected(false);
//    console.log("Disconnected");
//}

//function sendName() {
//    stompClient.publish({
//        destination: "/app/hello",
//        body: JSON.stringify({'clientName': $("#name").val()}),
//        headers: { username: userName }
//    });
//}

//function startGame() {
//    stompClient.publish({
//        destination: "/app/game/start/Danya",
//        body: JSON.stringify({'name': $("#gameName").val()}),
//        headers: { username: userName }
//    });
//}
//
//function listGames() {
//    stompClient.publish({
//        destination: "/app/games/list/Danya",
//        body: "{}",
//        headers: { username: userName }
//    });
//}
//
//function showGreeting(message) {
//    $("#greetings").append("<tr><td>" + message + "</td></tr>");
//}
//
//function showGames(games) {
//    for (game of games) {
//      $("#games").append("<tr><td>" + game.name + "</td></tr>");
//    }
//}
//
//function showMainMenu() {
//    $("#main-content").show()
//}

//$(function () {
////    $("form").on('submit', (e) => e.preventDefault());
////    $( "#connect" ).click(() => connect());
////    $( "#disconnect" ).click(() => disconnect());
////    $( "#send" ).click(() => sendName());
////    $( "#createGameButton" ).click(() => startGame());
////    $( "#listGames" ).click(() => listGames());
//});