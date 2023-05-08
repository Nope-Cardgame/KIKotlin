package entity.action

import entity.GameActionType
import entity.Player


/**
 * States, that a player is eliminated
 */
class EliminatedPlayerAction(
    type: GameActionType,
    explanation: String,
    player: Player
) : GameAction(type, explanation, player)