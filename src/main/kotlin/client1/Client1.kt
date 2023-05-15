package client1

import KotlinClientInterface
import LogConsoleFormatter
import NopeEventListener
import entity.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Client1-NopeClient
 *
 * Developer: [Jonas Pollpeter](https://github.com/JonasPTFL)
 */
class Client1(
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
        log.fine("sent game invite to players: ${startGameResult?.players}")
    }

    override fun socketConnected() {
        log.fine("socketConnected received")

        if (!invitedUser && usernameToInvite != null) {
            runBlocking {
                launch {
                    // wait until the other client is connected
                    delay(3000)
                    // Let client start the game. This will cause client1 to invite all players with name "kotlin" contained
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
    }

    override fun gameStateUpdate(game: Game) {
        log.fine("gameStateUpdate received")
        // check whether it is the clients turn
        if (game.currentPlayer.username == username) {
            val clientPlayer = game.currentPlayer
            // get current discard pile card (current card has index 0)
            val currentDiscardPileCard =
                game.discardPile.getOrNull(0) ?: throw Exception("discard pile is empty and game has not ended")
            val discardableCards = getDiscardableCards(currentDiscardPileCard, clientPlayer.cards)

            when(game.state) {
                GameState.GAME_START -> {
                    // game started
                }
                GameState.NOMINATE_FLIPPED -> {
                    // nominate flipped
                }
                GameState.CARD_DRAWN,
                GameState.TURN_START -> {
                    // check whether this client can discard any set of cards
                    if (discardableCards.isEmpty()) {
                        // check whether this client took a card from the discard pile in the previous action
                        if (game.state == GameState.CARD_DRAWN) {
                            // this client drew a card in the previous action, say nope, because the client can not draw any card
                            kotlinClientInterface.sayNope()
                        } else if (game.state == GameState.TURN_START) {
                            // this client did not draw a card last round and can not discard any set of cards
                            kotlinClientInterface.takeCard()
                        }
                    } else {
                        // discard first valid set
                        kotlinClientInterface.discardCard(discardableCards[0].take(currentDiscardPileCard.value))
                    }
                }
                GameState.GAME_END -> {
                    // game ended

                }
                GameState.CANCELLED -> {
                    // cancelled

                }
            }
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

        // print all players and their ranking
        println(game.players.joinToString { "${it.username}: ${it.ranking}." })
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

    /**
     * Finds all card sets of the same color, which contain enough cards to discard a specific part of them
     * @param currentDiscardPileCard current first discard pile card
     * @param hand hand of the client player
     * */
    private fun getDiscardableCards(currentDiscardPileCard: Card, hand: List<Card>): List<List<Card>> {
        return currentDiscardPileCard.colors.mapNotNull { requiredColor ->
            hand.filter { handCard ->
                // filter hand by cards matching the required color
                handCard.colors.contains(requiredColor)
            }.takeIf { setCandidate ->
                // filter card-set-candidates that matches the required amount of card, which is
                // given by the number value of the current discard pile
                setCandidate.size >= currentDiscardPileCard.value
            }
        }
    }
}