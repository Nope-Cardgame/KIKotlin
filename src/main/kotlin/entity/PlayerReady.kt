package entity

import com.google.gson.annotations.SerializedName


/**
 * Sent by this client, when this client is ready to play the game.
 * This should be sent along with the game event [Constants.WebSocket.EVENTS.READY]
 */
data class PlayerReady (
    val accept: Boolean,
    val type: ReadyGameType,
    val inviteId: String
)

enum class ReadyGameType {
    @SerializedName("game")
    GAME,
    @SerializedName("tournament")
    TOURNAMENT
}