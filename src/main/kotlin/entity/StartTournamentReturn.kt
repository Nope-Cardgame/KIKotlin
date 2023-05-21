package entity


/**
 * Represents the result returned by the start game api endpoint [Constants.API.START_TOURNAMENT]
 */
data class StartTournamentPostData(
    val mode: TournamentMode,
    val participants: List<Player>
)