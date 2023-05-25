package client1

import entity.Card
import entity.CardColor
import entity.CardType
import java.lang.IllegalStateException


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
     * @param currentDiscardPile current discard pile list
     * @param hand hand of the client player
     * */
    fun getDiscardableNumberCards(currentDiscardPile: List<Card>, hand: List<Card>): List<List<Card>> {
        if (currentDiscardPile.isEmpty()) return emptyList()

        val currentDiscardPileCard = currentDiscardPile[0]

        // check if the discard pile contains just the start card
        if (currentDiscardPile.size == 1) {
            return when(currentDiscardPileCard.type) {
                CardType.NUMBER -> {
                    // card value must exist for number cards checked by this case
                    findDiscardableCardSet(currentDiscardPileCard.colors, currentDiscardPileCard.value!!, hand)
                }
                CardType.NOMINATE -> throw IllegalStateException("nominate card should not be detected in getDiscardableNumberCards")
                CardType.RESET -> hand.filter { it.type == CardType.NUMBER }.map { listOf(it) }
                // recursively get discardable cards for previous card on the discard pile (card before invisible action card)
                CardType.INVISIBLE -> findDiscardableCardSet(currentDiscardPileCard.colors, 1, hand)
            }
        } else {
            return when(currentDiscardPileCard.type) {
                CardType.NUMBER -> {
                    // card value must exist for number cards checked by this case
                    findDiscardableCardSet(currentDiscardPileCard.colors, currentDiscardPileCard.value!!, hand)
                }
                CardType.NOMINATE -> throw IllegalStateException("nominate card should not be detected in getDiscardableNumberCards")
                CardType.RESET -> hand.filter { it.type == CardType.NUMBER }.map { listOf(it) }
                // recursively get discardable cards for previous card on the discard pile (card before invisible action card)
                CardType.INVISIBLE -> getDiscardableNumberCards(currentDiscardPile.subList(1,currentDiscardPile.size), hand)
            }
        }
    }

    fun findDiscardableCardSet(colors: List<CardColor>, amount: Int, hand: List<Card>): List<List<Card>> {
        return colors.mapNotNull { requiredColor ->
            hand.filter { handCard ->
                // filter hand by cards matching the required color
                handCard.type == CardType.NUMBER && handCard.colors.contains(requiredColor)
            }.takeIf { setCandidate ->
                // filter card-set-candidates that matches the required amount of card, which is
                // given by the number value of the current discard pile
                setCandidate.size >= amount
            }?.take(
                // TODO implement logic, that discards specific cards for a good reason and not just discard
                //  the first valid card set
                amount
            )
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