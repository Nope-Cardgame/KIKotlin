package entity


/**
 * Represents the result returned by the start game api endpoint
 */
data class StartGameReturn(
    val players: List<Player>
)