import entity.*
import entity.action.*
import io.socket.client.IO
import io.socket.client.Socket
import rest.LoginReturnData
import java.net.URI
import java.util.*


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
    private val loginReturnData: LoginReturnData,
    private val nopeEventListener: NopeEventListener
) : NopeGame {
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
            nopeEventListener.socketConnected()
        }

        socket.on(Constants.WebSocket.EVENTS.CONNECT_ERROR) {
            nopeEventListener.socketConnectError(it.firstOrNull().toString())
        }

        socket.on(Constants.WebSocket.EVENTS.DISCONNECT) {
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
                ready(game.id, ReadyGameType.GAME)
            }
        }
        onData<Tournament>(Constants.WebSocket.EVENTS.TOURNAMENT_INVITE) { tournament ->
            // notify listener about invitation
            val acceptInvitation = nopeEventListener.tournamentInvite(tournament)
            // only ready if the listener returned true
            if (acceptInvitation) {
                ready(tournament.id, ReadyGameType.TOURNAMENT)
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
    }

    /**
     * Calls the [Socket.on] method, serializes the received data array using the [SerializationHelper] class to be
     * sent as data parameter in the [listener] callback.
     * */
    private inline fun <reified T> onData(event: String, crossinline listener: (data: T) -> Unit) {
        socket.on(event) {
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

    override fun takeCard(explanation: String) {
        emitData(
            Constants.WebSocket.EVENTS.PLAY_ACTION,
            TakeCardAction(
                explanation = explanation
            )
        )
    }

    override fun discardCard(cards: List<Card>, explanation: String) {
        emitData(
            Constants.WebSocket.EVENTS.PLAY_ACTION,
            DiscardCardAction(
                explanation = explanation,
                cards = cards
            )
        )
    }

    override fun nominateCard(
        cards: List<Card>,
        nominatedPlayer: Player,
        nominatedColor: CardColor,
        explanation: String
    ) {
        emitData(
            Constants.WebSocket.EVENTS.PLAY_ACTION,
            NominateCardAction(
                explanation = explanation,
                cards = cards,
                nominatedPlayer = nominatedPlayer,
                nominatedColor = nominatedColor
            )
        )
    }

    override fun sayNope(explanation: String) {
        emitData(
            Constants.WebSocket.EVENTS.PLAY_ACTION,
            SayNopeAction(
                explanation
            )
        )
    }
}