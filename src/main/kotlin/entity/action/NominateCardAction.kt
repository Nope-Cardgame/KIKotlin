package entity.action

import entity.Card
import entity.CardColor
import entity.GameActionType
import entity.Player


/**
 * States, that a player nominated a card
 */
class NominateCardAction(
    type: GameActionType,
    explanation: String,
    player: Player,
    val cards: List<Card>,
    val nominatedPlayer: Player,
    val nominatedColor: CardColor
) : GameAction(type, explanation, player)