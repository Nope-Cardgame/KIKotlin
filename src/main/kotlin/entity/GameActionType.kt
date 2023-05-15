package entity

import com.google.gson.annotations.SerializedName

/**
 * Represents a game action type
 */
enum class GameActionType {
    @SerializedName("disqualify")
    DISQUALIFY,
    @SerializedName("take")
    TAKE,
    @SerializedName("discard")
    DISCARD,
    @SerializedName("nope")
    NOPE,
    @SerializedName("nominate")
    NOMINATE
}