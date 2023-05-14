package entity.action

import entity.GameActionType
import entity.Player


/**
 * States, that a player is disqualified
 */
class DisqualifiedPlayerAction(
    explanation: String,
    player: Player? = null
) : GameAction(GameActionType.disqualify, explanation, player)