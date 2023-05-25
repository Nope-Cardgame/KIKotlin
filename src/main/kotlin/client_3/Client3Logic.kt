package client_3

import entity.Card
import entity.CardType

class Client3Logic {

    /**
     * Method to find first matching list of cards to discard
     *
     * @return returns a list with the cards to discard from the hand.
     *          If list is empty not enough cards are available
     */
    fun checkForDiscard(hand: List<Card>, discardPileCard: Card): List<Card> {
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
                CardType.NOMINATE -> TODO()
                //Liegt eine Auswahlkarte zu Beginn des Spiels als erste offene Karte aus, führst
                //du als Startspieler die Aktion aus, als ob du die Karte selbst abgelegt hast
                //(Mitspieler bestimmen etc.).
                CardType.RESET -> {return listOf<Card>(hand[0])}
                CardType.INVISIBLE -> {
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
            if (counter >= discardPileCard.value) {
                return discardCards.subList(0, discardPileCard.value)
            }
        }
        return listOf<Card>()
    }

    fun checkInvisible(discardPileCard: List<Card>): Card {
        for((index, card) in discardPileCard.withIndex()) {
            if (card.type != CardType.INVISIBLE) {
                return card
            }
        }
        return discardPileCard[0]
    }


}