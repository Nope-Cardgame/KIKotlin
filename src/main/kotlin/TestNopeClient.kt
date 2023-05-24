import entity.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger


/**
 * NopeClient for testing purpose
 */
class TestNopeClient(
    private val username: String,
    password: String,
    private val usernameToInvite: String? = null
) : NopeEventListener {
    private val log = Logger.getLogger("${javaClass.name}/$username")

    // flag, that states, whether the usernameToInvite is already invited
    private var invitedUser: Boolean = false

    // instantiate interface and set this client as event listener
    private val kotlinClientInterface = KotlinClientInterface(
        username = username,
        password = password,
        nopeEventListener = this
    )

    init {
        // setup logger
        val consoleHandler = ConsoleHandler()
        consoleHandler.level = Level.ALL
        consoleHandler.formatter = LogConsoleFormatter()
        log.addHandler(consoleHandler)
        log.level = Level.ALL
        log.useParentHandlers = false
    }

    /**
     * Test method to let one client start the game
     * */
    private suspend fun startGame() {
        val userConnections = kotlinClientInterface.getUserConnections()
        // test game with kotlin client players only
        val playerToInvite = userConnections.first { it.username == usernameToInvite }
        val clientPlayer = userConnections.first { it.username == username }

        val startGameResult = kotlinClientInterface.startGame(
            StartGamePostData(
                noActionCards = true,
                noWildcards = false,
                oneMoreStartCards = false,
                players = listOf(playerToInvite, clientPlayer)
            )
        )
        invitedUser = true
        log.fine("sent game invite to players: ${startGameResult.players}")
    }

    override fun socketConnected() {
        log.fine("socketConnected received")

        if (!invitedUser && usernameToInvite != null) {
            runBlocking {
                launch {
                    // wait until the other client is connected
                    delay(3000)
                    // Let client start the game. This will cause client1 to invite the player with name usernameToInvite
                    startGame()
                }
            }
        }
    }

    override fun socketConnectError(error: String?) {
        log.fine("socketConnectError received")

    }

    override fun socketDisconnected() {
        log.fine("socketDisconnected received")
    }

    override fun disqualifiedPlayer(player: Player, explanation: String) {
        log.fine("disqualifiedPlayer received")
        log.fine("$player is disqualified because: $explanation")
    }

    override fun gameStateUpdate(game: Game) {
        log.fine("gameStateUpdate received")
        // check whether it is the client turn
        if (game.currentPlayer.username == username) {

            // take card by default
            kotlinClientInterface.takeCard()
        }
    }

    override fun communicationError(communicationError: CommunicationError) {
        log.fine("communicationError received")
    }

    override fun clientEliminated(playerEliminated: PlayerEliminated) {
        log.fine("clientEliminated received")
    }

    override fun gameEnd(game: Game) {
        log.fine("gameEnd received")
    }

    override fun tournamentEnd(tournament: Tournament) {
        log.fine("tournamentEnd received")
    }

    override fun gameInvite(game: Game): Boolean {
        log.fine("gameInvite received")
        // accept all invitations by default
        return true
    }

    override fun tournamentInvite(tournament: Tournament): Boolean {
        log.fine("tournamentInvite invoked(tournament: $tournament)")
        // accept all invitations by default
        return true
    }
}