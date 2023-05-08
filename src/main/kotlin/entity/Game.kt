package entity

import entity.action.GameAction


/**
 * Represents a nope game
 */
data class Game (
    val id: String,
    val state: GameState,
    val noActionCards: Boolean,
    val noWildcards: Boolean,
    val oneMoreStartCards: Boolean,
    val tournament: Tournament, // optional
    val type: GameType, // optional
    val encounterRound: Int,
    val players: List<Player>,
    val discardPile: List<Card>, // index 0 describes the first card on top of the pile
    val lastAction: GameAction,
    val currentPlayer: Player,
)