import entity.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger


/**
 * NopeClient for testing purpose
 */
class TestNopeClient(
    username: String = "kotlin",
    password: String = "kotlin"
) : NopeEventListener {
    private val log = Logger.getLogger(this.javaClass.name)

    // instantiate interface and set this client as event listener
    private val kotlinClientInterface = KotlinClientInterface(
        username = username,
        password = password,
        alreadySignedUp = true,
        nopeEventListener = this
    )

    init {
        val consoleHandler = ConsoleHandler()
        consoleHandler.level = Level.ALL
        log.addHandler(consoleHandler)
        log.level = Level.ALL
        log.useParentHandlers = false
    }

    /**
     * Test method to let one client start the game
     * */
    fun startGame() {
        runBlocking {
            launch {
                val userConnections = kotlinClientInterface.getUserConnections()
                // test game with kotlin client players only
                val invitePlayers = userConnections.filter { it.username.contains("kotlin") }

                kotlinClientInterface.startGame(
                    StartGamePostData(
                        noActionCards = true,
                        noWildcards = false,
                        oneMoreStartCards = false,
                        players = invitePlayers
                    )
                )
            }
        }
    }

    override fun socketConnected() {
        log.fine("socketConnected invoked")
    }

    override fun socketConnectError(error: String?) {
        log.fine("socketConnectError invoked (error: $error)")
    }

    override fun socketDisconnected() {
        log.fine("socketDisconnected invoked")
    }

    override fun disqualifiedPlayer(player: Player, explanation: String) {
        log.fine("disqualifiedPlayer invoked(player: $player, explanation: $explanation)")
    }

    override fun gameStateUpdate(game: Game) {
        log.fine("gameStateUpdate invoked(player: $game)")
    }

    override fun communicationError(communicationError: CommunicationError) {
        log.fine("communicationError invoked(communicationError: $communicationError)")
    }

    override fun clientEliminated(playerEliminated: PlayerEliminated) {
        log.fine("clientEliminated invoked(playerEliminated: $playerEliminated)")
    }

    override fun gameEnd(game: Game) {
        log.fine("gameEnd invoked(player: $game)")
    }

    override fun tournamentEnd(tournament: Tournament) {
        log.fine("tournamentEnd invoked(tournament: $tournament)")
    }

    override fun gameInvite(game: Game): Boolean {
        log.fine("gameInvite invoked(game: $game)")
        // accept all invitations by default
        return true
    }

    override fun tournamentInvite(tournament: Tournament): Boolean {
        log.fine("tournamentInvite invoked(tournament: $tournament)")
        // accept all invitations by default
        return true
    }
}