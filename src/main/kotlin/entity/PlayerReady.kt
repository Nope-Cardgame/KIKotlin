package entity

import com.google.gson.annotations.SerializedName


/**
 * Sent by this client, when this client is ready to play the game.
 * This should be sent along with the game event [Constants.WebSocket.EVENTS.READY]
 */
data class PlayerReady (
    val accept: Boolean,
    val type: GameType,
    @SerializedName("invite_id")
    val inviteId: Int
)

enum class ReadyGameType(private val value: String) {
    GAME("game"), TOURNAMENT("tournament");

    override fun toString() = value
}