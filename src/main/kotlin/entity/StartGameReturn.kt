package entity


/**
 * Represents the result returned by the start game api endpoint [Constants.API.START_GAME]
 */
data class StartGameReturn(
    val noActionCards: Boolean,
    val noWildcards: Boolean,
    val oneMoreStartCards: Boolean,
    val players: List<Player>
)