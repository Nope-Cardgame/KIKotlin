package client_3

import entity.*
import kotlin.math.min

class Client3Logic {

    /**
     * Method to find first matching list of cards to discard
     *
     * @return returns a list with the cards to discard from the hand.
     *          If list is empty not enough cards are available
     */
    fun checkForDiscard(hand: List<Card>, discardPileCard: Card, game: Game): List<Card> {
        val nextPlayer: Player = game.players.first { it.socketId != game.currentPlayer.socketId && (!it.disqualified)}
        //console print to track game
        println("++ discard Pile: ${discardPileCard.value} ${discardPileCard.name} ++")
        for ((index, card) in hand.withIndex()) {
            println("Card$index: nr. ${card.value} color ${card.name}")
        }
        //actionCard handling
        if (discardPileCard.type != CardType.NUMBER) {
            when(discardPileCard.type) {
                CardType.NUMBER -> {
                    println("shouldn't be here(check for discard->notNumber->Number)")
                }
                //creates temporary card to handle call normally(like a numbercard)
                CardType.NOMINATE -> {
                   var col: List<CardColor> = discardPileCard.colors
                    if (discardPileCard.colors.size > 1) {
                        col = listOf(game.lastNominateColor)
                    }
                    return checkForDiscard(hand, Card(CardType.NUMBER,game.lastNominateAmount,col , "Special NOMINATE"), game)}
                //plays first card in hand
                CardType.RESET -> {return listOf<Card>(hand[0])}
                //checks if there is a card in hand in this color to discard
                CardType.INVISIBLE -> { //** can only happen, if it is the startCard **
                    for (card in hand) {
                        for (color in card.colors) {
                            if (color == discardPileCard.colors[0]) {
                                return listOf<Card>(card)
                            }
                        }
                    }
                }
            }
        }
        //check hand for each color of discardPileCard
        for (color in discardPileCard.colors) {
            println("-----color: $color----")
            var discardCards = mutableListOf<Card>()
            var counter: Int = 0
            //checks each card of the hand
            for (handCard in hand) {
                //checks each color of the card from hand
                for (oneColor in handCard.colors) {
                    val comparing = oneColor == color
                    println("$comparing, ${handCard.name}")
                    if (comparing) {
                        counter++
                        discardCards.add(handCard)
                        println("++ $counter ++")
                        break
                    }
                }
            }
            //if the counter is higher, the current color is taken to discard

            if (counter >= discardPileCard.value!!) {
//                if (discardCards[0].type == CardType.NUMBER) {
//                    discardCards.removeAll { it.type != CardType.NUMBER }
                    discardCards.sortByDescending { it.colors.size } //{ it.colors.size * it.value!! }
//                }
                //sort by value * colorAmount or colorrating if first part is equal //TODO
//                if (discardCards.size > 1) {
//                    if (discardCards[0].type == CardType.NUMBER) {
//                        for (i in 0 until discardCards.size) {
//                            var value1 = (discardCards[0].value!! * discardCards[0].colors.size)
//                            if (discardCards[0].value == 3) {
//                                if (nextPlayer.cardAmount != null && nextPlayer.cardAmount <= 7) {
//                                    value1 -= 3
//                                }
//
//                            }
//                            var value2 = (discardCards[1].value!! * discardCards[1].colors.size)
//                            if (discardCards[1].value == 3) {
//                                if (nextPlayer.cardAmount != null && nextPlayer.cardAmount <= 7) {
//                                    value2 -= 3
//                                }
//                            }
//                            if (value1 < value2 ||
//                                value1 == value2 && getCardColorRating(discardCards[0], game) > getCardColorRating(
//                                    discardCards[1],
//                                    game
//                                )
//                            ) {
//                                val temp: Card = discardCards[0]
//                                discardCards = discardCards.subList(1, discardCards.lastIndex)
//                                discardCards.add(temp)
//                                break
//                            }
//                        }
//                    }
//                }
                    println(discardPileCard.value)
                return discardCards.subList(0, discardPileCard.value)
            }
        }
        return listOf<Card>()
    }

    fun checkInvisible(discardPileCard: List<Card>): Card {
        for(card in discardPileCard) {
            if (card.type != CardType.INVISIBLE) {
                return card
            }
        }
        return discardPileCard[0]
    }

    private fun getRemainingCards(searchedColor: CardColor, game: Game) :Int {
        var colorCards: Int = 0
        var allKnownCards = mutableListOf<Card>()
        allKnownCards = game.discardPile.toMutableList()
        allKnownCards.addAll(game.currentPlayer.cards)

        for (card in allKnownCards) {
            for (color in card.colors) {
                if (color == searchedColor) {
                    colorCards++
                    break
                }
            }
        }
        return colorCards
    }
    
    fun getCardColorRating(checkCard: Card, game: Game) : Int {
        var minVal: Int = 1000
        for (cardColor in checkCard.colors) {
            minVal = min(getRemainingCards(cardColor, game), minVal)
                
            }
        return minVal
        }

}