package entity


/**
 * Sent by this client, when this client is ready to play the game.
 * This should be sent along with the game event [Constants.WebSocket.EVENTS.READY]
 */
data class PlayerReady (
    val accept: Boolean,
    val type: ReadyGameType,
    val inviteId: String
)

enum class ReadyGameType(private val value: String) {
    game("game"), tournament("tournament");

    override fun toString() = value
}