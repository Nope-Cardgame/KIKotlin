import entity.*
import entity.action.GameAction
import io.socket.client.IO
import io.socket.client.Socket
import rest.LoginReturnData
import java.net.URI
import java.util.*
import java.util.logging.Logger


/**
 * Represents a web-socket connection using the socket-io library.
 * This class uses the [LoginReturnData] object returned by the webserver REST api after successful login
 * @param loginReturnData authentication data received from the rest login
 * @param nopeEventListener listener that should be notified when an event occurs
 * @see rest.RESTApi.signIn
 * @see rest.RESTApi.signUp
 *
 */
class SocketConnection(
    loginReturnData: LoginReturnData,
    private val nopeEventListener: NopeEventListener
) : NopeGame {
    private val log = Logger.getLogger(this.javaClass.name)
    private val serializationHelper = SerializationHelper()

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
        /********* technical *********/
        socket.on(Constants.WebSocket.EVENTS.CONNECT) {
            log.info("web-socket connected")
            nopeEventListener.socketConnected()
        }

        socket.on(Constants.WebSocket.EVENTS.CONNECT_ERROR) {
            log.severe("web-socket connect error occurred")
            nopeEventListener.socketConnectError(it.firstOrNull().toString())
        }

        socket.on(Constants.WebSocket.EVENTS.DISCONNECT) {
            log.warning("web-socket disconnected")
            nopeEventListener.socketDisconnected()
        }


        /********* structure *********/
        onData<PlayerEliminated>(Constants.WebSocket.EVENTS.ELIMINATED) { playerEliminated ->
            nopeEventListener.clientEliminated(playerEliminated)
        }
        onData<CommunicationError>(Constants.WebSocket.EVENTS.ERROR) { communicationError ->
            nopeEventListener.communicationError(communicationError)
        }


        /********* invitation *********/
        onData<Game>(Constants.WebSocket.EVENTS.GAME_INVITE) { game ->
            // notify listener about invitation
            val acceptInvitation = nopeEventListener.gameInvite(game)
            // only ready if the listener returned true
            if (acceptInvitation) {
                ready(game.id, ReadyGameType.game)
            }
        }
        onData<Tournament>(Constants.WebSocket.EVENTS.TOURNAMENT_INVITE) { tournament ->
            // notify listener about invitation
            val acceptInvitation = nopeEventListener.tournamentInvite(tournament)
            // only ready if the listener returned true
            if (acceptInvitation) {
                ready(tournament.id, ReadyGameType.tournament)
            }
        }


        /********* game related *********/
        onData<Game>(Constants.WebSocket.EVENTS.GAME_END) { game ->
            nopeEventListener.gameEnd(game)
        }
        onData<Tournament>(Constants.WebSocket.EVENTS.TOURNAMENT_END) { tournament ->
            nopeEventListener.tournamentEnd(tournament)
        }
        onData<Game>(Constants.WebSocket.EVENTS.GAME_STATE) { game ->
            nopeEventListener.gameStateUpdate(game)
        }
        onData<GameAction>(Constants.WebSocket.EVENTS.PLAY_ACTION) { action ->
            // TODO klären ob das wirklich gesendet werden soll, siehe NopeGame doc
        }
    }

    /**
     * Calls the [Socket.on] method, serializes the received data array using the [SerializationHelper] class to be
     * sent as data parameter in the [listener] callback.
     * Furthermore, this method logs the web socket event using the [log].
     * */
    private inline fun <reified T> onData(event: String, crossinline listener: (data: T) -> Unit) {
        socket.on(event) {
            log.info("web-socket event received: $event")
            listener(serializationHelper.deserialize(it))
        }
    }

    /**
     * Emits a socket event using the [Socket.emit] method with the data object as data
     * @param data object to be sent along with the socket event
     * */
    private fun <T> emitData(event: String, data: T) {
        socket.emit(event, serializationHelper.serialize(data))
    }

    /**
     * Creates a [PlayerReady] object with the given parameters and sends the [Constants.WebSocket.EVENTS.READY] event
     * using the socket
     * @param inviteId [Game.id] of the game object sent along with the invitation event
     * @param readyGameType game type for the invitation
     * */
    private fun ready(inviteId: String, readyGameType: ReadyGameType) {
        val ready = PlayerReady(
            accept = true,
            type = readyGameType,
            inviteId = inviteId
        )
        emitData(Constants.WebSocket.EVENTS.READY, ready)
    }

    /********* Implemented interface methods relate to [NopeGame] *********/

    override fun takeCard() {
        // TODO klären ob das wirklich gesendet werden soll, siehe NopeGame doc
    }

    override fun discardCard(card: Card) {
        // TODO klären ob das wirklich gesendet werden soll, siehe NopeGame doc
    }

    override fun nominateCard(card: Card) {
        // TODO klären ob das wirklich gesendet werden soll, siehe NopeGame doc
    }

    override fun sayNope() {
//        socket.emit(Constants.WebSocket.EVENTS.PLAY_ACTION, ) // TODO add data
    }
}