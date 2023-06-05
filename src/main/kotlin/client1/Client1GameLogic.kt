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

    companion object {

        /**
         * These values are integrated to the evaluator calculation when evaluating cards
         * */
        object Evaluator {
            /**
             * Factor by which the amount of colors of the card is multiplied
             * */
            const val CARD_COLOR_AMOUNT_WEIGHT_FACTOR: Float = 1.0f

            /**
             * Factor by which the amount of hand cards matching the card is multiplied
             * */
            const val MATCHING_CARD_AMOUNT_WEIGHT_FACTOR: Float = 0.0f
        }
    }

    /**
     * Assigns a number card an evaluated number
     *
     * @param evaluation higher evaluation means it is better to play the card in the current state
     * */
    data class EvaluatedNumberCard(
        val numberCard: Card,
        val evaluation: Float
    )

    /**
     * Evaluates a given card by the amount of colors and the amount of matching hand cards given by the parameter hand.
     * */
    private fun evaluateNumerCard(card: Card, hand: List<Card>): EvaluatedNumberCard {
        if (card.type != CardType.NUMBER) throw IllegalArgumentException("Card to evaluate must not be of type '${card.type}'")

        // sort card by color count to discard cards with more colors first
        // further more add the amount of matching hand cards to the filter. This prevents the player from
        // discarding a card on his own card, after no other player could discard
        val evaluationValue: Float =
            card.colors.size * Evaluator.CARD_COLOR_AMOUNT_WEIGHT_FACTOR -
                    hand.count { handCard -> handCard.colors.containsAny(card.colors) } * Evaluator.MATCHING_CARD_AMOUNT_WEIGHT_FACTOR
        return EvaluatedNumberCard(card, evaluationValue)
    }

    /**
     * Finds all card sets of the same color, which contain enough cards to discard a specific part of them
     * @param currentDiscardPile current discard pile list
     * @param hand hand of the client player
     * */
    fun getDiscardableNumberCards(currentDiscardPile: List<Card>, hand: List<Card>): List<Card> {
        val currentDiscardPileCard = currentDiscardPile[0]
        return when (currentDiscardPileCard.type) {
            CardType.NUMBER -> {
                findDiscardableNumberCardSet(currentDiscardPileCard.colors, currentDiscardPileCard.value!!, hand)
            }

            CardType.RESET -> {
                // determine card to discard and return empty list if no card found
                val cardToDiscard = determineDiscardCardForResetActionCard(hand) ?: return emptyList()
                return listOf(cardToDiscard) // return single card as list of card sets
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

    fun findDiscardableNumberCardSet(colors: List<CardColor>, amount: Int, hand: List<Card>): List<Card> {
        return colors.mapNotNull { requiredColor ->
            hand.filter { handCard ->
                // filter hand by cards matching the required color
                handCard.type == CardType.NUMBER && handCard.colors.contains(requiredColor)
            }.takeIf { setCandidate ->
                // filter card-set-candidates that matches the required amount of card, which is
                // given by the number value of the current discard pile
                setCandidate.size >= amount
            }
                ?.map { evaluateNumerCard(it, hand) }
                ?.sortedByDescending { it.evaluation }
                ?.take(amount)
        }
            // sort valid card candidate sets by the evaluated value of the first card (this card will be on discard pile top)
            .maxByOrNull { it[0].evaluation }?.map { it.numberCard } ?: emptyList()
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
        discardableActionCards.addAll(actionCardsReset)
        discardableActionCards.addAll(actionCardsInvisible)

        return discardableActionCards
    }

    /**
     * Determines the best card to discard for a reset action card event
     * */
    private fun determineDiscardCardForResetActionCard(hand: List<Card>): Card? {
        // get card with the most colors
        val maxColorsCard = hand.filter { it.type == CardType.NUMBER }.maxByOrNull { it.colors.size }
        if (maxColorsCard != null) return maxColorsCard

        // determine the best non number card to discard
        // check least valuable invisible cards at first and sort them by color amount descending
        val invisibleCards = hand.filter { it.type == CardType.INVISIBLE }.sortedByDescending { it.colors.size }
        if (invisibleCards.isNotEmpty()) return invisibleCards.first()

        // check reset cards and sort them by color amount descending
        val resetCards = hand.filter { it.type == CardType.RESET }.sortedByDescending { it.colors.size }
        if (resetCards.isNotEmpty()) return resetCards.first()

        // check most valuable nominate cards and sort them by color amount descending
        val nominateCards = hand.filter { it.type == CardType.RESET }.sortedBy { it.colors.size }
        if (nominateCards.isNotEmpty()) return nominateCards.first()

        return null
    }

    /**
     * Determines the best player to select as nominated player in the current state
     * */
    fun determineNominatedPlayer(game: Game, clientPlayer: Player): Player {
        // get player with the least card amount, which is either disqualified nor the client player
        return game.players.filter { it.socketId != clientPlayer.socketId && !it.disqualified }.minBy { it.cards.size }
    }

    /**
     * Determines the best color to select as nominated color in the current state
     * */
    fun determineNominatedColor(game: Game, clientPlayer: Player): CardColor {
        val cardColorsCount = mutableMapOf<CardColor, Int>()
        clientPlayer.cards.forEach {
            it.colors.forEach { cardColor ->
                // increase card color counter by 1
                cardColorsCount[cardColor] = cardColorsCount.getOrDefault(cardColor, 0) + 1
            }
        }

        // determine card color with the least occurrences in the client players hand cards
        return cardColorsCount.minBy { it.value }.key
    }

    /**
     * Determines the best amount to select as nominated amount in the current state
     * @return int in interval [1,3]
     * */
    fun determineNominatedAmount(game: Game, clientPlayer: Player, nominatedPlayer: Player): Int {
        if (game.discardPile[0].hasAllColors() && nominatedPlayer.cardAmount != null && nominatedPlayer.cardAmount >= 3)
            return 3

        // count matching number cards, that could be discarded by the client player, if no other player can
        // discard a card for this nominate request
        val clientPlayerHasMatchingCard =
            clientPlayer.cards.count { it.type == CardType.NUMBER && it.colors.containsAny(game.discardPile[0].colors) }
        if (clientPlayerHasMatchingCard < 3 && nominatedPlayer.cardAmount != null && nominatedPlayer.cardAmount >= 3) {
            // if player has less than 3 matching cards, determine nominate amount 3
            return 3
        }
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