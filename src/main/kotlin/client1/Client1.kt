package client1

import KotlinClientInterface
import LogConsoleFormatter
import NopeEventListener
import entity.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.LoginCredentials
import java.lang.IllegalStateException
import java.util.logging.FileHandler
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
        val LOG_LEVEL: Level = Level.ALL

        object Config {
            const val ACCEPT_GAME_INVITATION_DEFAULT = true
            const val ACCEPT_TOURNAMENT_INVITATION_DEFAULT = true

            /**
             * States whether the client should accept all invitations automatically without any user input
             * */
            const val ACCEPT_INVITATION_AUTOMATICALLY = true // changed for debugging purpose, was false

            object DefaultGame {
                const val ACTION_CARDS_ENABLED = true
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
        val consoleHandler = FileHandler("./log.txt")
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
                clientPlayer = connectedUsers.first { it.socketId == kotlinClientInterface.getClientSocketID() },
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
        log.fine("sent game invite to socket ids: ${startGameResult.players.joinToString { it.socketId }}")
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

            log.info("game: $game")

            when (game.state) {
                GameState.GAME_START -> {
                    // game started
                }
                GameState.NOMINATE_FLIPPED -> {
                    // nominate flipped
                }
                GameState.CARD_DRAWN,
                GameState.TURN_START -> {
                    // get current discard pile card (current card has index 0)
                    val currentDiscardPileCard =
                        game.discardPile.getOrNull(0) ?: throw Exception("discard pile is empty and game has not ended")
                    val discardableNumberCards = gameLogic.getDiscardableNumberCards(currentDiscardPileCard, clientPlayer.cards)
                    val discardableActionCards = gameLogic.getDiscardableActionCards(currentDiscardPileCard, clientPlayer.cards)
                    log.info("discardableNumberCards: $discardableNumberCards")
                    log.info("discardableActionCards: $discardableActionCards")

                    when {
                        // check whether this client can discard any set of cards
                        discardableNumberCards.isNotEmpty() -> {
                            // discard first valid set
                            // TODO implement logic, that discards specific cards for a good reason and not just discard
                            //  the first valid card set
                            kotlinClientInterface.discardCard(discardableNumberCards[0].take(currentDiscardPileCard.value))
                            log.info(loginCredentials.username + " discarded card: " + discardableNumberCards[0])
                        }
                        // check whether this client can discard any action card
                        discardableActionCards.isNotEmpty() -> {
                            // discard first valid set
                            // TODO implement logic, that discards specific action card and not the first one
                            when (discardableActionCards[0].type) {
                                CardType.NOMINATE -> {
                                    // test call use nominate card
                                    kotlinClientInterface.nominateCard(
                                        cards = listOf(discardableActionCards[0]),
                                        nominatedPlayer = game.players.first { it.socketId != clientPlayer.socketId }, // find first non-client player
                                        nominatedColor = CardColor.BLUE, // static color choice
                                        nominatedAmount = 1 // static amount choice
                                    )
                                    log.info(loginCredentials.username + " discarded action card: " + discardableActionCards[0])
                                }
                                CardType.RESET, CardType.INVISIBLE -> {
                                    // test call use other actions cards
                                    kotlinClientInterface.discardCard(cards = listOf(discardableActionCards[0]))
                                    log.info(loginCredentials.username + " discarded action card: " + discardableActionCards[0])
                                }

                                else -> {
                                    throw IllegalStateException("Card type ${discardableActionCards[0].type} is not implemented for element in discardableActionCards")
                                }
                            }
                        }
                        else -> {
                            // check whether this client took a card from the discard pile in the previous action
                            if (game.state == GameState.CARD_DRAWN) {
                                // this client drew a card in the previous action, say nope, because the client can not draw any card
                                kotlinClientInterface.sayNope()
                            } else if (game.state == GameState.TURN_START) {
                                // this client did not draw a card last round and can not discard any set of cards
                                kotlinClientInterface.takeCard()
                            }
                        }
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
        log.fine("communicationError received: (message: ${communicationError.message})")
    }

    override fun clientEliminated(playerEliminated: PlayerEliminated) {
        log.fine("clientEliminated received: (reason: ${playerEliminated.reason}, disqualified: ${playerEliminated.disqualified})")
    }

    override fun gameEnd(game: Game) {
        log.fine("gameEnd received")

        // print all players sorted by their ranking
        val playerListResult =
            game.players.sortedBy { it.ranking }.joinToString(separator = "\n") { it.getEndGameStringFormat() }
        println("Game end (GameID: ${game.id}). Result ranking:")
        println(playerListResult)

        // allow user to start a new game after one game finished
        runBlocking {
            launch {
                askToInviteUsers()
            }
        }
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