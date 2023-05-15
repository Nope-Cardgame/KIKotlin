package entity.action

import entity.GameActionType
import entity.Player


/**
 * States, that a player said nope
 */
class SayNopeAction(
    explanation: String,
    player: Player? = null
) : GameAction(GameActionType.NOPE, explanation, player)