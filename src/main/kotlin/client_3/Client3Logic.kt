package client_3

import entity.Card
import entity.CardColor
import entity.CardType
import entity.Game

class Client3Logic {

    /**
     * Method to find first matching list of cards to discard
     *
     * @return returns a list with the cards to discard from the hand.
     *          If list is empty not enough cards are available
     */
    fun checkForDiscard(hand: List<Card>, discardPileCard: Card, game: Game): List<Card> {
        //console print to track game
        println("++ discard Pile: ${discardPileCard.value} ${discardPileCard.name} ++")
        for ((index, card) in hand.withIndex()) {
            println("Card$index: nr. ${card.value} color ${card.name}")
        }

        if (discardPileCard.type != CardType.NUMBER) {
            when(discardPileCard.type) {
                CardType.NUMBER -> {
                    println("shouldn't be here(check for discard->notNumber->Number)")
                }
                CardType.NOMINATE -> {return checkForDiscard(hand, Card(CardType.NUMBER,game.lastNominateAmount, listOf(game.lastNominateColor), "Special NOMINATE"), game)}
                CardType.RESET -> {return listOf<Card>(hand[0])}
                CardType.INVISIBLE -> {
                    //** can only happen, if it is the startCard **
                    //checks if there is a card in hand in this color to discard
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
            val discardCards = mutableListOf<Card>()
            var counter: Int = 0
            //checks each card of the hand
            for (handCard in hand) {
                //checks each color of the card

                for (oneColor in handCard.colors) {
                    val comparing =oneColor == color
                    println("$comparing, ${handCard.name}")
                    if (comparing) {
                        counter++
                        discardCards.add(handCard)
                        println("++ $counter ++")
                        break
                    }
                }
            }
            if (counter >= discardPileCard.value!!) {
                discardCards.sortByDescending {it.colors.size}
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


}