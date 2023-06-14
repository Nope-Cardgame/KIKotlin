import entity.*
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
     * Returns the clients current socket id
     * */
    fun getClientSocketID() = socketConnection.getSocketID()

    /**
     * Disconnects the socket
     * */
    fun disconnectSocket() = socketConnection.disconnectSocket()

    /**
     * Invites the players and tries to start the game
     * @param startGamePostData configures the game, that should be started
     *
     * @return game configuration from the server
     * */
    suspend fun startGame(startGamePostData: StartGamePostData) = restApi.startGame(startGamePostData)

    /**
     * Starts a tournament and invites the given players
     * @param startTournamentPostData configures the tournament, that should be started
     *
     * @return tournament object from the server
     * */
    suspend fun startTournament(startTournamentPostData: StartTournamentPostData) =
        restApi.startTournament(startTournamentPostData)

    /**
     * Searches for a played game with the given id
     * */
    suspend fun getGame(gameId: String) = restApi.getGame(gameId)

    /**
     * Returns all played games
     * */
    suspend fun getGames() = restApi.getGames()

    /**
     * Searches for a played tournament with the given id
     * */
    suspend fun getTournament(tournamentId: String) = restApi.getTournament(tournamentId)

    /**
     * Returns all played tournaments
     * */
    suspend fun getTournaments() = restApi.getTournaments()


    /**** delegating client actions to the socket connection ****/
    override fun takeCard(explanation: String) = socketConnection.takeCard(explanation)
    override fun discardCard(cards: List<Card>, explanation: String) = socketConnection.discardCard(cards, explanation)
    override fun nominateCard(
        cards: List<Card>,
        nominatedPlayer: Player,
        nominatedColor: CardColor?,
        nominatedAmount: Int,
        explanation: String
    ) = socketConnection.nominateCard(cards, nominatedPlayer, nominatedColor, nominatedAmount, explanation)

    override fun sayNope(explanation: String) = socketConnection.sayNope(explanation)

}