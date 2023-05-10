package entity


/**
 * States, that this client-player is eliminated/banned. Sent by the [Constants.WebSocket.EVENTS.ELIMINATED] game event
 */
data class PlayerEliminated(
    val reason: String,
    val disqualified: Boolean
)