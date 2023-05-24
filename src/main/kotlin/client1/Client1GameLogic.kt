package client1

import entity.Card
import entity.CardType


/**
 * Contains the internal business game logic for the Client1 KI
 * Provides methods to decide the next moves and game actions and stores game related data
 * to calculate specific actions.
 *
 * @author [Jonas Pollpeter](https://github.com/JonasPTFL)
 */
internal class Client1GameLogic {

    /**
     * Finds all card sets of the same color, which contain enough cards to discard a specific part of them
     * @param currentDiscardPileCard current first discard pile card
     * @param hand hand of the client player
     * */
    fun getDiscardableNumberCards(currentDiscardPileCard: Card, hand: List<Card>): List<List<Card>> {
        return currentDiscardPileCard.colors.mapNotNull { requiredColor ->
            hand.filter { handCard ->
                // filter hand by cards matching the required color
                handCard.type == CardType.NUMBER && handCard.colors.contains(requiredColor)
            }.takeIf { setCandidate ->
                // filter card-set-candidates that matches the required amount of card, which is
                // given by the number value of the current discard pile
                setCandidate.size >= currentDiscardPileCard.value
            }
        }
    }
    /**
     * Finds playable action card
     * */
    fun getDiscardableActionCards(currentDiscardPileCard: Card, hand: List<Card>): List<Card> {
        val discardableActionCards = mutableListOf<Card>()

        val actionCardsNominate = hand.filter { it.type == CardType.NOMINATE && currentDiscardPileCard.colors.containsAny(it.colors) }
        val actionCardsInvisible = hand.filter { it.type == CardType.INVISIBLE && currentDiscardPileCard.colors.containsAny(it.colors) }
        val actionCardsReset = hand.filter { it.type == CardType.RESET }
        discardableActionCards.addAll(actionCardsNominate)
        discardableActionCards.addAll(actionCardsInvisible)
        discardableActionCards.addAll(actionCardsReset)

        return discardableActionCards
    }
}