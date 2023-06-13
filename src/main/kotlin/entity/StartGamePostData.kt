package entity


/**
 * Represents the result returned by the start game api endpoint [Constants.API.START_GAME]
 */
data class StartGamePostData(
    val noActionCards: Boolean,
    val noWildCards: Boolean,
    val oneMoreStartCards: Boolean,
    val actionTimeout: Int? = null, // 1-120 seconds
    val invitationTimeout: Int? = null, // 1-600 seconds
    val startWithRejection: Boolean? = null,
    val players: List<Player>
)