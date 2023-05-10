import entity.StartGamePostData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.RESTApi


fun main() {
    val username = "kotlin"
    val password = "kotlin"
    val username2 = "kotlin2"
    val password2 = "kotlin2"
    val restApi = RESTApi()
    val testNopeClient = TestNopeClient()

    runBlocking {
        launch {
//            val loginResult = restApi.signUp(username, password) // already signed up
            val loginResult = restApi.signIn(username, password)
            val loginResult2 = restApi.signIn(username2, password2)

            val socketConnection = SocketConnection(loginResult, testNopeClient)
            val socketConnection2 = SocketConnection(loginResult2, testNopeClient)

            // necessary to avoid sending rest request before socket connection
            // TODO should be improved in future
            delay(1000)

            val userConnections = restApi.userConnections()

            val startGamePostData = restApi.startGame(
                StartGamePostData(
                    noActionCards = true,
                    noWildcards = false,
                    oneMoreStartCards = false,
                    players = userConnections.filter { it.username.contains(username) }
                )
            )
        }
    }
}