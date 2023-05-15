package client1

import KotlinClientInterface
import LogConsoleFormatter
import NopeEventListener
import entity.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.LoginCredentials
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Client1-NopeClient
 * This class lets the user control the client using the console and reacts to server events by delegating logic to the
 * [Client1GameLogic] and obtaining information about the nope game using the kotlin client api class
 * [KotlinClientInterface].
 *
 * @author [Jonas Pollpeter](https://github.com/JonasPTFL)
 */
class Client1 : NopeEventListener {
    private val log = Logger.getLogger(javaClass.name)

    private val loginCredentials: LoginCredentials
    private val kotlinClientInterface: KotlinClientInterface
    private val gameLogic = Client1GameLogic()

    companion object {
        val LOG_LEVEL: Level = Level.OFF

        object Config {
            const val ACCEPT_GAME_INVITATION_DEFAULT = false
            const val ACCEPT_TOURNAMENT_INVITATION_DEFAULT = false

            /**
             * States whether the client should accept all invitations automatically without any user input
             * */
            const val ACCEPT_INVITATION_AUTOMATICALLY = false

            object DefaultGame {
                const val ACTION_CARDS_ENABLED = false
                const val WILD_CARDS_ENABLED = true
                const val ONE_MORE_START_CARDS_ENABLED = true
            }

            object Console {
                const val BOOL_TRUE = "y"
                const val BOOL_FALSE = "n"
                const val SPLIT_DELIMITER = ","
            }

        }

    }

    init {
        println("-- Nope Client1 started --")

        setupLogger()

        // get credentials input
        loginCredentials = getCredentialsInput()

        // instantiate interface and set this client as event listener
        kotlinClientInterface = KotlinClientInterface(
            username = loginCredentials.username,
            password = loginCredentials.password,
            nopeEventListener = this
        )
    }

    /**
     * Sets up the logger
     * */
    private fun setupLogger() {
        val consoleHandler = ConsoleHandler()
        consoleHandler.level = LOG_LEVEL
        consoleHandler.formatter = LogConsoleFormatter()
        log.addHandler(consoleHandler)
        log.level = LOG_LEVEL
        log.useParentHandlers = false
    }

    /**
     * Reads the user credentials input from the console and returns them as [LoginCredentials]
     * */
    private fun getCredentialsInput(): LoginCredentials {
        val username = readln("Please enter a username:")
        val password = readln("Please enter a password:")
        return LoginCredentials(username = username, password = password)
    }


    /**
     * Prints all connected users to the console and asks the user to invite users and start a new nope game
     * */
    private suspend fun askToInviteUsers() {
        // print connected users in format "{index+1} {username} (SocketID: {})"
        val connectedUsers = kotlinClientInterface.getUserConnections()
        val userListBuilder = StringBuilder()
        connectedUsers.forEachIndexed { index, player ->
            userListBuilder.append("[${index + 1}] ${player.username} (SocketID: ${player.socketId})\n")
        }
        println("Currently connected users:")
        print(userListBuilder)
        // ask user to invite players
        val userIndicesToInvite = readIntList(
            "Input the numbers (comma-separated) of the users you want to invite to play a nope game with or enter nothing to skip: "
        )

        if (userIndicesToInvite.isEmpty()) {
            println("Inviting users skipped")
        } else {
            // obtain game setting input
            val actionCardsEnabled =
                readBool("Enable action cards y/n (default ${Config.DefaultGame.ACTION_CARDS_ENABLED.toConsoleStringRepresentation()}): ")
                    ?: Config.DefaultGame.ACTION_CARDS_ENABLED
            val wildCardsEnabled =
                readBool("Enable wild cards y/n (default ${Config.DefaultGame.WILD_CARDS_ENABLED.toConsoleStringRepresentation()}): ")
                    ?: Config.DefaultGame.WILD_CARDS_ENABLED
            val oneMoreStartCardsEnabled =
                readBool("Enable one more start cards y/n (default ${Config.DefaultGame.ONE_MORE_START_CARDS_ENABLED.toConsoleStringRepresentation()}): ")
                    ?: Config.DefaultGame.ONE_MORE_START_CARDS_ENABLED

            // filter users matching the index list input and start game
            startGame(
                usersToInvite = connectedUsers.filterIndexed { index, _ ->
                    // filter index+1, because the numbers printed to the console start from 1
                    userIndicesToInvite.contains(index + 1)
                },
                clientPlayer = connectedUsers.first { it.username == loginCredentials.username }, // TODO get client user by socket id
                noActionCards = !actionCardsEnabled,
                noWildcards = !wildCardsEnabled,
                oneMoreStartCards = oneMoreStartCardsEnabled,
            )
        }
    }


    /**
     * Starts a new nope game and invites the users
     *
     * @param usersToInvite users that will be invited to the new nope game
     * @param clientPlayer player object representing this client player
     * */
    private suspend fun startGame(
        usersToInvite: List<Player>,
        clientPlayer: Player,
        noActionCards: Boolean,
        noWildcards: Boolean,
        oneMoreStartCards: Boolean,
    ) {
        val startGameResult = kotlinClientInterface.startGame(
            StartGamePostData(
                noActionCards = noActionCards,
                noWildcards = noWildcards,
                oneMoreStartCards = oneMoreStartCards,
                players = usersToInvite.plus(clientPlayer)
            )
        )
        log.fine("sent game invite to socket ids: ${startGameResult?.players?.joinToString { it.socketId }}")
    }

    /**** Overridden Websocket Events ****/
    override fun socketConnected() {
        log.fine("socketConnected received")
        println("Client connected")

        runBlocking {
            launch {
                askToInviteUsers()
            }
        }
    }

    override fun socketConnectError(error: String?) {
        log.fine("socketConnectError received")

        println("Client connect error: $error")
    }

    override fun socketDisconnected() {
        log.fine("socketDisconnected received")

        println("Client disconnected")
    }

    override fun disqualifiedPlayer(player: Player, explanation: String) {
        log.fine("disqualifiedPlayer received")
    }

    override fun gameStateUpdate(game: Game) {
        log.fine("gameStateUpdate received")
        // check whether it is the clients turn
        if (game.currentPlayer.username == loginCredentials.username) {
            val clientPlayer = game.currentPlayer
            // get current discard pile card (current card has index 0)
            val currentDiscardPileCard =
                game.discardPile.getOrNull(0) ?: throw Exception("discard pile is empty and game has not ended")
            val discardableCards = gameLogic.getDiscardableCards(currentDiscardPileCard, clientPlayer.cards)

            when (game.state) {
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

        // print all players sorted by their ranking
        val playerListResult =
            game.players.sortedBy { it.ranking }.joinToString(separator = "\n") { it.getEndGameStringFormat() }
        println("Game end - result:")
        println(playerListResult)
    }

    override fun tournamentEnd(tournament: Tournament) {
        log.fine("tournamentEnd received")
    }

    override fun gameInvite(game: Game): Boolean {
        log.fine("gameInvite received")

        // automatically accept if defined in config
        if (Config.ACCEPT_INVITATION_AUTOMATICALLY) return true

        return readBool(
            "Game invitation received (GameID: ${game.id}). " +
                    "Accept y/n (default ${Config.ACCEPT_GAME_INVITATION_DEFAULT.toConsoleStringRepresentation()}): "
        ) ?: Config.ACCEPT_GAME_INVITATION_DEFAULT
    }

    override fun tournamentInvite(tournament: Tournament): Boolean {
        log.fine("tournamentInvite invoked(tournament: $tournament)")

        // automatically accept if defined in config
        if (Config.ACCEPT_INVITATION_AUTOMATICALLY) return true

        return readBool(
            "Tournament invitation received (TournamentID: ${tournament.id}). " +
                    "Accept y/n (default ${Config.ACCEPT_TOURNAMENT_INVITATION_DEFAULT.toConsoleStringRepresentation()}): "
        ) ?: Config.ACCEPT_TOURNAMENT_INVITATION_DEFAULT
    }
}