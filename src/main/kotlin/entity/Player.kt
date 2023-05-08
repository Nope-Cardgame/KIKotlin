package entity


/**
 * Represents a nope player
 */
data class Player (
    val name: String,
    val websocketID: String,
    val cardAmount: Int,
    val cards: List<Card>,
    val score: Int
)