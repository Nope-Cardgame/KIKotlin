package client1


/**
 * Provides methods for console I/O
 */

/**
 * Prints the description to the console and reads the users input line in the same console line
 * @param description text to be printed before executing [readln]
 * */
fun readln(description: String): String {
    print(description)
    return readln()
}

/**
 * Reads the user input with a description
 * @param description text to be printed before executing [readln]
 * @return true if the input equals the [Client1.Companion.Config.Console.BOOL_TRUE] or false if the input equals
 * [Client1.Companion.Config.Console.BOOL_FALSE] and null if none of these constants matches
 * */
fun readBool(description: String): Boolean? {
    return when (readln(description)) {
        Client1.Companion.Config.Console.BOOL_TRUE -> true
        Client1.Companion.Config.Console.BOOL_FALSE -> false
        else -> null
    }
}

/**
 * Reads the user input, splits it up using the delimiter [Client1.Companion.Config.Console.SPLIT_DELIMITER] and converts every
 * valid int-representation to int type.
 * @return list containing all numbers that could be converted to int
 * */
fun readIntList(description: String): List<Int> {
    return readln(description).split(Client1.Companion.Config.Console.SPLIT_DELIMITER).mapNotNull { it.toIntOrNull() }
}