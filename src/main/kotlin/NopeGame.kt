import entity.Card
import entity.CardColor
import entity.Player

/**
 * Interface for the client to play a nope game
 */
interface NopeGame {

    /**
     * Take a card from the draw pile
     * */
    fun takeCard(explanation: String = "")

    /**
     * Discard a card to the discard pile
     * @param cards cards to be discarded
     * */
    fun discardCard(cards: List<Card>, explanation: String = "")

    /**
     * Nominate a player
     * @param cards
     * */
    fun nominateCard(
        cards: List<Card>,
        nominatedPlayer: Player,
        nominatedColor: CardColor,
        nominatedAmount: Int,
        explanation: String = ""
    )

    /**
     * Says nope
     * */
    fun sayNope(explanation: String = "")
}