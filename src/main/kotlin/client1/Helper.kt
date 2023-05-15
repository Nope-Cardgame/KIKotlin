package client1

import entity.Player


/**
 * File that contains helper methods
 */

/**
 * Converts this boolean value to its representing string constant defined in [Client1.Companion.Config.Console]
 * @return if this is true [Client1.Companion.Config.Console.BOOL_TRUE] else [Client1.Companion.Config.Console.BOOL_FALSE]
 * */
fun Boolean.toConsoleStringRepresentation() =
    if (this) Client1.Companion.Config.Console.BOOL_TRUE else Client1.Companion.Config.Console.BOOL_FALSE

fun Player.getEndGameStringFormat() = "$username (SocketID: $socketId, disqualified: $disqualified, cards: $cardAmount, ranking: $ranking)"
 