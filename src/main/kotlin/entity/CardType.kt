package entity

import com.google.gson.annotations.SerializedName

/**
 * Represents a nope card type
 */
enum class CardType {
    @SerializedName("number")
    NUMBER,
    @SerializedName("nominate")
    NOMINATE,
    @SerializedName("reset")
    RESET,
    @SerializedName("invisible")
    INVISIBLE,
}