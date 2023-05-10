package entity


/**
 * Represents aa error sent by the [Constants.WebSocket.EVENTS.ERROR] game event
 */
data class CommunicationError(val message: String)