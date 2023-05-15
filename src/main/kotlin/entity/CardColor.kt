package entity

import com.google.gson.annotations.SerializedName


/**
 * Represents a color of a nope card
 */
enum class CardColor {
    @SerializedName("red")
    RED,
    @SerializedName("green")
    GREEN,
    @SerializedName("blue")
    BLUE,
    @SerializedName("yellow")
    YELLOW
}