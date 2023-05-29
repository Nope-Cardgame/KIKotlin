package entity


/**
 * Represents a nope card
 */
data class Card(
    val type: CardType,
    val value: Int? = null,
    val colors: List<CardColor>,
    val name: String
)
