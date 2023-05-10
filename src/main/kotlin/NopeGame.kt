import entity.Card

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
    fun takeCard()

    /**
     * Discard a card to the discard pile
     * @param card card to be discarded
     * */
    fun discardCard(card: Card)

    /**
     * Nominate a card. TODO noch nicht verstanden was das macht
     * @param card TODO
     * */
    fun nominateCard(card: Card)

    /**
     * Says nope
     * */
    fun sayNope()
}