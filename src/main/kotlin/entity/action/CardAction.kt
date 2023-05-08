package entity.action

import entity.Card
import entity.GameActionType
import entity.Player


/**
 * States, that a player took or discarded a card
 */
class CardAction(
    type: GameActionType,
    explanation: String,
    player: Player,
    val amount: Int,
    val cards: List<Card>
) : GameAction(type, explanation, player)