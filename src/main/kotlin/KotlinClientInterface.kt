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
 *
 * This class automatically authenticates to the REST api and the web-socket using the given credentials.
 * If the account is not already signed up, this class will automatically sign up the user.
 */
class KotlinClientInterface(
    username: String,
    password: String,
    nopeEventListener: NopeEventListener
) : NopeGame {

    private val restApi = RESTApi()
    private lateinit var socketConnection: SocketConnection

    init {
        runBlocking {
            launch {
                // try sign-in with credentials and fallback to signup process
                // when an error occurred and the user is not already registered
                val loginResult = restApi.signIn(username, password) ?: restApi.signUp(username, password)

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