package entity

/**
 * Represents the mode of a tournament
 */
data class TournamentMode(
    val name: String,
    val numberOfRounds: Int,
    val pointsPerGameWin: Boolean
)