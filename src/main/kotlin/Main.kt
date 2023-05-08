import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.RESTApi


fun main() {
    println("--- Softwareprojekt-KIKotlin ---")

    val username = "kotlin"
    val password = "kotlin"
    val restApi = RESTApi()

    runBlocking {
        launch {
//            val loginResult = restApi.signUp(username, password) // already signed up
            val loginResult = restApi.signIn(username, password)

            val socketConnection = SocketConnection(loginResult)

            val startGameReturn = restApi.startGame()
            println("startGameReturn: ${startGameReturn?.players}")

            val userConnections = restApi.userConnections()
            println("userConnections: $userConnections")
        }
    }
}