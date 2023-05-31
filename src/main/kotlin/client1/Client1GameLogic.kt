package client1

import entity.*


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
        val currentDiscardPileCard = currentDiscardPile[0]
        return when (currentDiscardPileCard.type) {
            CardType.NUMBER -> {
                findDiscardableNumberCardSet(currentDiscardPileCard.colors, currentDiscardPileCard.value!!, hand)
            }

            CardType.RESET -> {
                // determine card to discard and return empty list if no card found
                val cardToDiscard = determineDiscardCardForResetActionCard(hand) ?: return emptyList()
                return listOf(listOf(cardToDiscard)) // return single card as list of card sets
            }

            CardType.NOMINATE -> {
                // nominate card should be handled separately in TURN_START event
                throw IllegalStateException("unhandled nominate card detected on discard pile")
            }

            CardType.INVISIBLE -> {
                if (currentDiscardPile.size == 1) {
                    // edge case invisible card as first card on the discard pile on game start
                    // TODO is it possible to discard action cards with matching color in this edge case too?
                    findDiscardableNumberCardSet(currentDiscardPileCard.colors, 1, hand)
                } else {
                    // nominate card should only be detected as first and single card on the discard pile
                    throw IllegalStateException("invisible card detected on the discard pile with more than one card")
                }
            }
        }
    }

    fun findDiscardableNumberCardSet(colors: List<CardColor>, amount: Int, hand: List<Card>): List<List<Card>> {
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
     * Finds playable action card for the given colors
     * */
    fun getDiscardableActionCards(colors: List<CardColor>, hand: List<Card>): List<Card> {
        val discardableActionCards = mutableListOf<Card>()

        val actionCardsNominate = hand.filter { it.type == CardType.NOMINATE && colors.containsAny(it.colors) }
        val actionCardsInvisible = hand.filter { it.type == CardType.INVISIBLE && colors.containsAny(it.colors) }
        val actionCardsReset = hand.filter { it.type == CardType.RESET }
        discardableActionCards.addAll(actionCardsNominate)
        discardableActionCards.addAll(actionCardsInvisible)
        discardableActionCards.addAll(actionCardsReset)

        return discardableActionCards
    }

    private fun determineDiscardCardForResetActionCard(hand: List<Card>): Card? {
        // TODO implement logic to determine best card to discard on reset action card
        return hand.firstOrNull()
    }

    /**
     * Determines the best player to select as nominated player in the current state
     * */
    fun determineNominatedPlayer(game: Game, clientPlayer: Player): Player {
        // TODO implement logic to determine best player
        return game.players.first { it.socketId != clientPlayer.socketId && !it.disqualified }
    }

    /**
     * Determines the best color to select as nominated color in the current state
     * */
    fun determineNominatedColor(game: Game, clientPlayer: Player): CardColor {
        // TODO implement logic to determine best color
        return CardColor.RED
    }

    /**
     * Determines the best amount to select as nominated amount in the current state
     * @return int in interval [1,3]
     * */
    fun determineNominatedAmount(game: Game, clientPlayer: Player): Int {
        // TODO implement logic to determine best amount
        return 1
    }

    /**
     * starting from the top, this method removes all invisible cards from the discard pile, until a non-invisible card
     * is found at the top or the discard pile has only one card left
     * */
    fun removeTopInvisibleCards(discardPile: List<Card>): List<Card> {
        val currentCard = discardPile.getOrNull(0) ?: throw Exception("discard pile is empty and game has not ended")
        return if (discardPile.size > 1 && currentCard.type == CardType.INVISIBLE)
            removeTopInvisibleCards(discardPile.subList(1, discardPile.size))
        else
            discardPile
    }
}