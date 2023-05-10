import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val username = "kotlin"
    val password = "kotlin"
    val username2 = "kotlin2"
    val password2 = "kotlin2"

    // instantiate client
    val testNopeClient1 = TestNopeClient(
        username = username,
        password = password
    )
    val testNopeClient2 = TestNopeClient(
        username = username2,
        password = password2
    )


    runBlocking {
        launch {
            // delay is necessary to avoid sending rest request before socket connection completed
            // TODO should be improved in future
            delay(1000)
            // Let client1 start the game. This will cause client1 to invite all players with name "kotlin" contained
            testNopeClient1.startGame()
        }
    }

}