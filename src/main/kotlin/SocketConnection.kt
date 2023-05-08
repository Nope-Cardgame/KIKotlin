import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import java.util.*
import java.util.logging.Logger

/**
 * Represents a web-socket connection using the socket-io library.
 * This class uses the [LoginReturnData] object returned by the webserver REST api after successful login
 * @see RESTApi.signIn
 */
class SocketConnection(loginReturnData: LoginReturnData) {
    private val log = Logger.getLogger(this.javaClass.name)

    // server web-socket uri
    private val serverURI: URI = URI.create(Constants.WebSocket.URL)

    // options used for the connection
    private val socketOptions = IO.Options.builder()
        .setAuth(Collections.singletonMap(Constants.WebSocket.AUTH_TOKEN_KEY, loginReturnData.jsonWebToken))
        .build()

    // socket io object
    private val socket: Socket = IO.socket(serverURI, socketOptions)

    init {
        registerEvents()
        socket.connect()
    }

    /**
     * Registers all necessary events to be listened to by the web-socket connection
     * */
    private fun registerEvents() {
        socket.on(Constants.WebSocket.EVENTS.CONNECT) {
            log.info("web-socket connected")
        }

        socket.on(Constants.WebSocket.EVENTS.CONNECT_ERROR) {
            log.severe("web-socket connect error occurred")
        }

        socket.on(Constants.WebSocket.EVENTS.DISCONNECT) {
            log.warning("web-socket disconnected")
        }

        // TODO implement all events
    }
}