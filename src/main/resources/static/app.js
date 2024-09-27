


const { createApp, ref, onMounted, computed } = Vue

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/qwe?username='
});


createApp({
  setup() {

    const username = ref('')
    const gameId = ref('')
    const newGameName = ref('')

    const connected = ref(false)
    const mainScreen = ref('login')
    const showMainMenu = computed(() => mainScreen.value == 'main-menu')
    const showGameScreen = computed(() => mainScreen.value == 'game')
    const games = ref([])


    const onWsConnect = (frame) => {
      console.log('Connected: ' + frame);

      stompClient.subscribe('/user/topic/greeting', (message) => {
          let body = JSON.parse(message['body'])
          console.log(body)
          if (body.activeGame) {
            console.log("Game already started");
            gameId.value = body.activeGame
            mainScreen.value = 'game'
//            showGame(body.activeGame);
          } else {
            mainScreen.value = 'main-menu'
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
              let found = false
              games.value.forEach((item, i) => {
                if (item.id == key) {
                  games.value[i] = value;
                  found = true;
                }
              });
              if (!found) {
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
          destination: `/app/game/${gameId.value}/leave`,
          body: "{}",
          headers: { username: username.value }
      });
    }

    const createGame = () => {
      stompClient.publish({
          destination: "/app/game/start",
          body: JSON.stringify({'name': newGameName.value}),
          headers: { username: username }
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

//    onMounted(() => {
//      username.value = 'Danya'
//      connect()
//    })


    const disconnect = () => {
        stompClient.deactivate();
        connected.value = false
    }



    const message = ref('Hello vue!')
    return {
      connected,
      username,
      connect,
      disconnect,
      showMainMenu,
      showGameScreen,
      message,

      games,
      join,
      leave,

      newGameName,
      createGame
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