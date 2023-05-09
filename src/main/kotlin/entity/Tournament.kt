package entity

/**
 * Represents a tournament info object.
 * This class is not yet explained in detail according to the project documentation
 */
class Tournament(
    val id: Long,
    val mode: TournamentMode,
    val participants: List<TournamentParticipant>,
    val games: List<Game>,
    val startTime: String,
    val endTime: String
)