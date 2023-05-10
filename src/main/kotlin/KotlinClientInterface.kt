import entity.Card
import entity.StartGamePostData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.RESTApi

/**
 * Interface for the individual kotlin clients to play a nope game.
 *
 * Unites the functionality of the [RESTApi] and the [SocketConnection] classes to one API to allow
 * simple access to the nope game for each kotlin client.
 */
class KotlinClientInterface(
    username: String,
    password: String,
    alreadySignedUp: Boolean,
    nopeEventListener: NopeEventListener
) : NopeGame {

    private val restApi = RESTApi()
    private lateinit var socketConnection: SocketConnection

    init {
        runBlocking {
            launch {
                val loginResult =
                    if (alreadySignedUp) restApi.signIn(username, password)
                    else restApi.signUp(username, password)

                // build socket connection
                socketConnection = SocketConnection(loginResult, nopeEventListener)
            }
        }
    }


    /**
     * Returns all players connected to the socket
     * */
    suspend fun getUserConnections() = restApi.userConnections()

    /**
     * Invites the players and tries to start the game
     * @param startGamePostData configures the game, that should be started
     *
     * @return game configuration from the server
     * */
    suspend fun startGame(startGamePostData: StartGamePostData) = restApi.startGame(startGamePostData)

    /**** delegating client actions to the socket connection ****/
    override fun takeCard() = socketConnection.takeCard()
    override fun discardCard(card: Card) = socketConnection.discardCard(card)
    override fun nominateCard(card: Card) = socketConnection.nominateCard(card)
    override fun sayNope() = socketConnection.sayNope()
}