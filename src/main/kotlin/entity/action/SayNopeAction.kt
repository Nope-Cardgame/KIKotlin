package entity.action

import entity.GameActionType
import entity.Player


/**
 * States, that a player said nope
 */
class SayNopeAction(
    type: GameActionType,
    explanation: String,
    player: Player
) : GameAction(type, explanation, player)