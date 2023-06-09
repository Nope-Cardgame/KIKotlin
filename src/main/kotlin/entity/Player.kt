package entity


/**
 * Represents a nope player
 */
data class Player (
    val username: String,
    val socketId: String,
    val cardAmount: Int? = null,
    val cards: List<Card>,
    val ranking: Int,
    val disqualified: Boolean,
)