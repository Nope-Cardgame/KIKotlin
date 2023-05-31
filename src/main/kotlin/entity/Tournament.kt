package entity

/**
 * Represents a tournament info object.
 * This class is not yet explained in detail according to the project documentation
 */
class Tournament(
    val id: String,
    val mode: TournamentMode,
    val state: TournamentState,
    val noActionCards: Boolean,
    val noWildCards: Boolean,
    val oneMoreStartCards: Boolean,
    val actionTimeout: Int, // 1-120 seconds
    val invitationTimeout: Int, // 1-600 seconds
    val startWithRejection: Boolean,
    val sendGameInvite: Boolean,
    val participantAmount: Int,
    val participants: List<TournamentParticipant>,
    val gameAmount: Int,
    val games: List<Game>,
    val startTime: String,
    val endTime: String
)