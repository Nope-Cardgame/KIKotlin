package client_3

import entity.Card
import entity.CardType

class ClientLogic {

    /**
     * Method to find first matching list of cards to discard
     *
     * @return returns a list with the cards to discard from the hand.
     *          If list is empty not enough cards are available
     */
    fun checkForDiscard(hand: List<Card>, discardPileCard: Card): List<Card> {
        if (discardPileCard.type == CardType.NUMBER) {
            //check hand for each color of discardPileCard
            for (color in discardPileCard.colors) {
                val discardCards = mutableListOf<Card>()
                var counter: Int = 0
                //checks each card of the hand
                for (handCard in hand) {
                    //checks each color of the card
                    for (oneColor in handCard.colors) {
                        if (oneColor == color) {
                            counter = +1
                            discardCards.add(handCard)
                            break
                        }
                    }
                }
                if (counter >= discardPileCard.value) {
                    return discardCards.subList(0, discardPileCard.value)
                }
            }
        }
        return listOf<Card>()
    }


}