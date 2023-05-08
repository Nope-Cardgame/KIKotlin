package entity


/**
 * States, that this client-player is banned. Sent by the [Constants.WebSocket.EVENTS.BANNED] game event
 */
data class PlayerBanned(val reason: String)