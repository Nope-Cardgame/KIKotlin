package entity


/**
 * Represents the result returned by the start game api endpoint [Constants.API.START_TOURNAMENT]
 */
data class StartTournamentReturn(
    val mode: TournamentMode,
    val players: List<Player>
)