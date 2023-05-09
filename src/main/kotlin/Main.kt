import entity.StartGameReturn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.RESTApi


fun main() {
    println("--- Softwareprojekt-KIKotlin ---")

    val username = "kotlin"
    val password = "kotlin"
    val username2 = "kotlin2"
    val password2 = "kotlin2"
    val restApi = RESTApi()

    runBlocking {
        launch {
//            val loginResult = restApi.signUp(username, password) // already signed up
            val loginResult = restApi.signIn(username, password)
            val loginResult2 = restApi.signIn(username2, password2)

            val socketConnection = SocketConnection(loginResult)
            val socketConnection2 = SocketConnection(loginResult2)

            // necessary to avoid sending rest request before socket connection
            // TODO should be improved in future
            delay(1000)

            val userConnections = restApi.userConnections()
            println("userConnections: $userConnections")

            val startGameReturn = restApi.startGame(
                StartGameReturn(
                    noActionCards = true,
                    noWildcards = false,
                    oneMoreStartCards = false,
                    players = userConnections.filter { it.username.contains(username) }
                )
            )
            println("startGameReturn: $startGameReturn")
        }
    }
}