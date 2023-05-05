import dev.icerock.moko.socket.Socket
import dev.icerock.moko.socket.SocketEvent
import dev.icerock.moko.socket.SocketOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    println("--- Softwareprojekt-KIKotlin ---")

    val username = "kotlin"
    val password = "kotlin"
    val restApi = RESTApi()

    runBlocking {
        launch {
//            val loginResult = restApi.signUp(username, password) // already signed up
            val loginResult = restApi.signIn(username, password)
            println("JSON web token: ${loginResult.jsonWebToken}")
        }
    }


    val socket = Socket(
        endpoint = "",
        config = SocketOptions(
            queryParams = mapOf("token" to "MySuperToken"),
            transport = SocketOptions.Transport.WEBSOCKET
        )
    ) {
        on(SocketEvent.Connect) {
            println("connect")
        }

        on(SocketEvent.Connecting) {
            println("connecting")
        }

        on(SocketEvent.Disconnect) {
            println("disconnect")
        }

        on(SocketEvent.Error) {
            println("error $it")
        }

        on(SocketEvent.Reconnect) {
            println("reconnect")
        }

        on(SocketEvent.ReconnectAttempt) {
            println("reconnect attempt $it")
        }

        on(SocketEvent.Ping) {
            println("ping")
        }

        on(SocketEvent.Pong) {
            println("pong")
        }

        on("employee.connected") { data ->
            println(data)
        }
    }
}