package entity.action

import entity.Card
import entity.CardColor
import entity.GameActionType
import entity.Player


/**
 * States, that a player nominated a card
 */
class NominateCardAction(
    explanation: String,
    player: Player? = null,
    val cards: List<Card>,
    val amount: Int? = null,
    val nominatedPlayer: Player,
    val nominatedColor: CardColor,
    val nominatedAmount: Int
) : GameAction(GameActionType.NOMINATE, explanation, player)