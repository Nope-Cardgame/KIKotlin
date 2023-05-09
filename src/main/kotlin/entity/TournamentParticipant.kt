package entity

/**
 * Represents a tournament participant
 */
data class TournamentParticipant (
    val username: String,
    val ranking: Int,
    val disqualified: Boolean,
    val score: Int,
)