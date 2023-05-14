package entity.action

import entity.GameActionType
import entity.Player

/**
 * Superclass for all game actions
 * */
open class GameAction(
    val type: GameActionType,
    val explanation: String,
    val player: Player? = null
)