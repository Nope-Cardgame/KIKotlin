package entity


/**
 * Represents the result returned by the start game api endpoint [Constants.API.START_TOURNAMENT]
 */
data class StartTournamentPostData(
    val mode: TournamentMode,
    val noActionCards: Boolean,
    val noWildCards: Boolean,
    val oneMoreStartCards: Boolean,
    val actionTimeout: Int, // 1-120 seconds
    val invitationTimeout: Int, // 1-600 seconds
    val startWithRejection: Boolean,
    val sendGameInvite: Boolean,
    val participants: List<Player>
)