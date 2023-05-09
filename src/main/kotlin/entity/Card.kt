package entity


/**
 * Represents a nope card
 */
data class Card(
    val type: CardType,
    val value: Int,
    val color: List<CardColor>,
    val name: String
)