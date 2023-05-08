package entity.action

import entity.GameActionType
import entity.Player

/**
 * Superclass for all game actions
 * */
sealed class GameAction(
    val type: GameActionType,
    val explanation: String,
    val player: Player
)