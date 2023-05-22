package entity

/**
 * Represents a tournament participant
 */
data class TournamentParticipant (
    val username: String,
    val socketId: String,
    val ranking: Int,
    val disqualified: Boolean,
    val score: Int,
)