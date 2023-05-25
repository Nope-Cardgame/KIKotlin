package client1

import entity.Player
import entity.TournamentParticipant


/**
 * File that contains helper methods
 */

/**
 * Converts this boolean value to its representing string constant defined in [Client1.Companion.Config.Console]
 * @return if this is true [Client1.Companion.Config.Console.BOOL_TRUE] else [Client1.Companion.Config.Console.BOOL_FALSE]
 * */
fun Boolean.toConsoleStringRepresentation() =
    if (this) Client1.Companion.Config.Console.BOOL_TRUE else Client1.Companion.Config.Console.BOOL_FALSE

fun Player.getEndGameStringFormat() =
    "$username (SocketID: $socketId, disqualified: $disqualified, cards: $cardAmount, ranking: $ranking)"

fun TournamentParticipant.getEndGameStringFormat() =
    "$username (SocketID: $socketId, disqualified: $disqualified, score: ${this.score}, ranking: $ranking)"

/**
 * Checks whether this list contains any of the given elements
 * @return true, if any element of the given list [elements] is contained in this list
 * */
fun <T> List<T>.containsAny(elements: List<T>): Boolean {
    return !none { elements.contains(it) }
}
 