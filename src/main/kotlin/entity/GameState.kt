package entity

import com.google.gson.annotations.SerializedName

/**
 * Represents a nope game state
 */
enum class GameState {
    @SerializedName("game_start")
    GAME_START,
    @SerializedName("nominate_flipped")
    NOMINATE_FLIPPED,
    @SerializedName("turn_start")
    TURN_START,
    @SerializedName("card_drawn")
    CARD_DRAWN,
    @SerializedName("cancelled")
    CANCELLED,
    @SerializedName("game_end")
    GAME_END
}