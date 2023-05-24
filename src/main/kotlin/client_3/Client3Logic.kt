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
        println("discard: ${discardPileCard.value} ${discardPileCard.name}")
        for (card in hand) {
            println("nr. ${card.value} color ${card.name}")
        }
        if (discardPileCard.type != CardType.NUMBER) {
            return listOf()
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


}