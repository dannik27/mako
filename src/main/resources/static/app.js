


const { createApp, ref, onMounted, computed } = Vue

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/qwe?username='
});


createApp({
  setup() {

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


    const onGameStateReceived = (message) => {
        console.log("Game state received: " + message['body'])
        let newState = JSON.parse(message['body'])

        let currentPlayerIndex = newState['players'].findIndex(player => player.name == username.value)
        console.log(currentPlayerIndex)

        playerState.value = newState['players'][currentPlayerIndex]
        opponents.value = newState['players'].toSpliced(currentPlayerIndex, 1)

        activePlayer.value = newState.activePlayer

//        .filter(player => player.name != username.value)
    }

    const onWsConnect = (frame) => {
      console.log('Connected: ' + frame);

      stompClient.subscribe('/user/topic/greeting', (message) => {
          let body = JSON.parse(message['body'])
          console.log(body)
          if (body.activeGame) {
            currentGame.value = body.activeGame
            console.log("Game already started");
            stompClient.subscribe(`/topic/game/${currentGame.value.id}/state`, onGameStateReceived);



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
//            showGame(body.activeGame);
          } else {
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
          console.log("Games received: " + JSON.parse(message['body'])['games'])
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

//      stompClient.subscribe('/user/topic/game/' + activeGameId + "/state", (message) => {
//          let body = JSON.parse(message['body'])
//          if (body.status === 'lobby') {
//              console.log("lobby")
//          }
//          if (body.status === 'turn') {
//              console.log("turn")
//          }
//      });

      stompClient.publish({
          destination: "/app/hello",
          body: JSON.stringify({'clientName': username.value}),
          headers: { username: username.value }
      });

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

    const connect = () => {
        if (username.value == null || username.value.length == 0) {
          console.log("Username must not be empty")
          return
        }
        console.log("qqq " + username.value)
        stompClient.onConnect = onWsConnect
        stompClient.activate();
    }

    onMounted(() => {
      username.value = 'qwe'
      connect()
    })


    const disconnect = () => {
        stompClient.deactivate();
        connected.value = false
    }


    const diceRoll = () => {
        stompClient.publish({
            destination: `/app/session/${currentGame.value.id}/dice`,
            body: "{}",
            headers: { username: username.value }
        });
    }

    const buyCard = (cardName) => {
        stompClient.publish({
            destination: `/app/session/${currentGame.value.id}/buy`,
            body: JSON.stringify({ name: cardName}),
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

      games,
      join,
      leave,

      newGameName,
      createGame,
      currentGame,
      startGame,

      playerState,
      opponents,
      activePlayer,

      diceRoll,
      buyCard
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