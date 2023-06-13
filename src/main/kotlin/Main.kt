import client1.Client1
import client3.Client3Orga

fun main() {
    println("Wähle die Startoption aus:\n0:Alle Clients parallel ausführen (führt zu Vermischungen der Ausgaben in der Konsole)\n1:Client1\n3:Client3")

    // start program in reference to the user input
    when (readln().toIntOrNull()) {
        null,
        0 -> {
            runClient3()
            runClient1()
        }

        1 -> runClient1()
        3 -> runClient3()
    }
}

/**
 * Executes Client1 program
 * */
fun runClient1() {
    val client1 = Client1()
}

/**
 * Executes Client3 program
 * */
fun runClient3() {
    val username3 = "jan"
    val password3 = "jan"

    val client3 = Client3Orga(
        username = username3,
        password = password3
    )
}