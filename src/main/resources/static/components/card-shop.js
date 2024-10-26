



const cardShopComponent = async () => {
  let template = await fetch("components/card-shop.html")
  template = await template.text()
  return ({
    template: template,
    props: ['lastDice', 'playerState', 'cards', 'opponents', 'gameId'],
    setup(props) {

      let cards = props.cards
      let playerState = props.playerState
      let opponents = props.opponents
      let gameId = props.gameId


    const cardsLeftCount = (cardName) => {
      let card = cards.find(c => c.name == cardName)
      if (card.color == "YELLOW" || card.color == "PURPLE") {
        return playerState.cards.hasOwnProperty(cardName) ? 0 : 1
      } else {

        let occupied = 0

        opponents.forEach(op => {
          let occupiedByOpponent = op.cards[cardName]
          if (occupiedByOpponent) {
            occupied += occupiedByOpponent
          }
        })

        let occupiedByPlayer = playerState.cards[cardName]
        if (occupiedByPlayer) {
          occupied += occupiedByPlayer
        }

        if (card.startCard) {
          occupied -= 1
          occupied -= opponents.length
        }

        return 6 - occupied
      }

    }

    const canBuyCard = (cardName) => {
      let card = cards.find(c => c.name == cardName)
      return card.price <= playerState.money && cardsLeftCount(cardName) > 0
    }

    const cardsByColor = (color) => {
      return cards.filter(c => c.color == color && cardsLeftCount(c.name) > 0).toSorted((a, b) => {
        let valueA = a.numbers ? a.numbers[0] : a.price
        let valueB = b.numbers ? b.numbers[0] : b.price
        return valueA - valueB
        }).map(c => ({
          name: c.name,
          icon: c.type,
          price: c.price,
          cardsLeft: cardsLeftCount(c.name),
          numbers: c.numbers == null ? '' : c.numbers.length == 1 ? c.numbers[0] : c.numbers[0] + '-' + c.numbers[c.numbers.length - 1],
          canBuy: canBuyCard(c.name)
        }))

    }

    const buyCard = (cardName) => {
        if (canBuyCard(cardName)) {
          stompClient.publish({
              destination: `/app/session/${gameId}/buy`,
              body: JSON.stringify({ name: cardName}),
              headers: { username: playerState.name }
          });
        }
    }

    const skipBuild = () => {
        stompClient.publish({
            destination: `/app/session/${gameId}/skip-build`,
            body: "{}",
            headers: { username: playerState.name }
        });
    }

    const addToFund = () => {
        stompClient.publish({
            destination: `/app/session/${gameId}/fund`,
            body: "{}",
            headers: { username: playerState.name }
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

      onMounted(() => {
            var flkty = new Flickity( '.shop-carousel', {
              "setGallerySize": false,
              "pageDots": false,
              "prevNextButtons": false,
              "cellAlign": "center",
              "wrapAround": true
            });
          })

      const qq = ref('qwe')

      return {
        qq,
        cardsLeftCount,
        canBuyCard,
        cardsByColor,
        shopGroups,
        buyCard,
        skipBuild,
        addToFund
      }
    }
  })
}

