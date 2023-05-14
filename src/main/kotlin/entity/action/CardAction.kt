package entity.action

import entity.Card
import entity.GameActionType
import entity.Player


/**
 * States, that a player took or discarded a card
 */
open class CardAction(
    type: GameActionType,
    explanation: String,
    player: Player? = null,
    val amount: Int? = null,
    val cards: List<Card>? = null
) : GameAction(type, explanation, player)

class TakeCardAction(
    explanation: String,
    player: Player? = null,
    amount: Int? = null,
    cards: List<Card>? = null
) : CardAction(GameActionType.take, explanation, player, amount, cards)

class DiscardCardAction(
    explanation: String,
    player: Player? = null,
    amount: Int? = null,
    cards: List<Card>
) : CardAction(GameActionType.discard, explanation, player, amount, cards)