import entity.Card
import entity.CardColor
import entity.Player

/**
 * Interface for the client to play a nope game
 * TODO klären wie actions (takeCard, discardCard, nominateCard, say nope) funktionieren, soll da ein CardAction/SayNopeAction Objekt gesendet werden?
 *  ergibt doch mehr sinn das zu empfangen, wie aber wird dann die abzulegende Karte übermittelt?!
 *  -> und wie funktioniert die disqualifyPlayer action? die muss noch im NopeEventListener richtig aufgerufen werden
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
     * Nominate a card. TODO noch nicht verstanden was das macht
     * @param cards TODO
     * */
    fun nominateCard(cards: List<Card>, nominatedPlayer: Player, nominatedColor: CardColor, explanation: String = "")

    /**
     * Says nope
     * */
    fun sayNope(explanation: String = "")
}