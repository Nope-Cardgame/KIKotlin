package client1

import KotlinClientInterface
import LogConsoleFormatter
import NopeEventListener
import entity.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rest.LoginCredentials
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
            const val START_TOURNAMENT = true // determines, whether a tournament should be started instead of a single game
            const val ASK_TO_INVITE_ON_CONNECT = true
            const val ASK_TO_INVITE_AFTER_GAME_FINISHED = false
            const val ACCEPT_GAME_INVITATION_DEFAULT = true
            const val ACCEPT_TOURNAMENT_INVITATION_DEFAULT = true

            /**
             * States whether the client should accept all invitations automatically without any user input
             * */
            const val ACCEPT_INVITATION_AUTOMATICALLY = true // changed for debugging purpose, was false

            object DefaultGame {
                const val ACTION_TIMEOUT = 10
                const val INVITATION_TIMEOUT = 10
                const val START_WITH_REJECTION = false
                const val TOURNAMENT_SEND_GAME_INVITE = false
                const val ACTION_CARDS_ENABLED = true
                const val WILD_CARDS_ENABLED = true
                const val ONE_MORE_START_CARDS_ENABLED = true
                const val TOURNAMENT_MODE_NAME = "round-robin"
                const val TOURNAMENT_NUMBER_OF_ROUNDS = 5
                const val TOURNAMENT_POINTS_PER_GAME_WIN = true
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
            // start game
            val clientPlayer = connectedUsers.first { it.socketId == kotlinClientInterface.getClientSocketID() }
            // filter users matching the index list input and start game
            val usersToInvite = connectedUsers.filterIndexed { index, _ ->
                // filter index+1, because the numbers printed to the console start from 1
                userIndicesToInvite.contains(index + 1)
            }
            if (Config.START_TOURNAMENT) {
                // start tournament
                val startTournamentResult = kotlinClientInterface.startTournament(
                    StartTournamentPostData(
                        mode = TournamentMode(
                            name = Config.DefaultGame.TOURNAMENT_MODE_NAME,
                            numberOfRounds = Config.DefaultGame.TOURNAMENT_NUMBER_OF_ROUNDS,
                            pointsPerGameWin = Config.DefaultGame.TOURNAMENT_POINTS_PER_GAME_WIN
                        ),
                        noActionCards = !Config.DefaultGame.ACTION_CARDS_ENABLED,
                        noWildCards = !Config.DefaultGame.WILD_CARDS_ENABLED,
                        oneMoreStartCards = Config.DefaultGame.ONE_MORE_START_CARDS_ENABLED,
                        actionTimeout = Config.DefaultGame.ACTION_TIMEOUT,
                        invitationTimeout = Config.DefaultGame.INVITATION_TIMEOUT,
                        startWithRejection = Config.DefaultGame.START_WITH_REJECTION,
                        sendGameInvite = Config.DefaultGame.TOURNAMENT_SEND_GAME_INVITE,
                        participants = usersToInvite.plus(clientPlayer)
                    )
                )

                println("Players invited (gameId: ${startTournamentResult.participants.joinToString { it.socketId }})")
                log.fine("sent game invite to socket ids: ${startTournamentResult.participants.joinToString { it.socketId }}")
            } else {
                // start single game
                val startGameResult = kotlinClientInterface.startGame(
                    StartGamePostData(
                        noActionCards = !Config.DefaultGame.ACTION_CARDS_ENABLED,
                        noWildCards = !Config.DefaultGame.WILD_CARDS_ENABLED,
                        oneMoreStartCards = Config.DefaultGame.ONE_MORE_START_CARDS_ENABLED,
                        actionTimeout = Config.DefaultGame.ACTION_TIMEOUT,
                        invitationTimeout = Config.DefaultGame.INVITATION_TIMEOUT,
                        startWithRejection = Config.DefaultGame.START_WITH_REJECTION,
                        players = usersToInvite.plus(connectedUsers.first { it.socketId == kotlinClientInterface.getClientSocketID() })
                    )
                )

                println("Players invited (gameId: ${startGameResult.players.joinToString { it.socketId }})")
                log.fine("sent game invite to socket ids: ${startGameResult.players.joinToString { it.socketId }}")
            }
        }
    }

    /**** Overridden Websocket Events ****/
    override fun socketConnected() {
        log.fine("socketConnected received")
        println("Client connected")

        if (Config.ASK_TO_INVITE_ON_CONNECT) {
            runBlocking {
                launch {
                    askToInviteUsers()
                }
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
        // check whether it is the clients turn
        if (game.currentPlayer.username == loginCredentials.username) {
            val clientPlayer = game.currentPlayer

            log.info("game: $game")

            when (game.state) {
                GameState.GAME_START -> {
                    log.fine("game state GAME_START")
                    // game started
                    println("Game started (gameId: ${game.id}, startPlayer: ${game.currentPlayer.username})")
                }

                GameState.NOMINATE_FLIPPED -> {
                    log.fine("game state NOMINATE_FLIPPED")
                    // game started and the first card on the discard pile is a nominate action card
                    // this allows the current player to nominate a player as if he played this nominate card
                    val canChooseColor = game.discardPile[0].hasAllColors()
                    kotlinClientInterface.nominateCard(
                        cards = emptyList(),
                        nominatedPlayer = gameLogic.determineNominatedPlayer(game, clientPlayer),
                        // TODO only send color when canChooseColor. The sever currently ignores this to be set
                        nominatedColor = gameLogic.determineNominatedColor(game, clientPlayer),
                        nominatedAmount = gameLogic.determineNominatedAmount(game, clientPlayer)
                    )
                }

                GameState.CARD_DRAWN,
                GameState.TURN_START -> {
                    log.fine("game state CARD_DRAWN | TURN_START")
                    val discardPile = gameLogic.removeTopInvisibleCards(game.discardPile)
                    // get current discard pile card (current card has index 0)
                    val currentDiscardPileCard =
                        discardPile.getOrNull(0) ?: throw Exception("discard pile is empty and game has not ended")

                    if (currentDiscardPileCard.type == CardType.NOMINATE) {
                        handleNominateCard(game, currentDiscardPileCard, clientPlayer)
                    } else {
                        val discardableNumberCards =
                            gameLogic.getDiscardableNumberCards(discardPile, clientPlayer.cards)
                        val discardableActionCards =
                            gameLogic.getDiscardableActionCards(currentDiscardPileCard.colors, clientPlayer.cards)

                        when {
                            // check whether this client can discard any action card
                            discardableActionCards.isNotEmpty() -> {
                                discardBestActionCard(discardableActionCards, game, clientPlayer)
                            }
                            // check whether this client can discard any set of cards
                            discardableNumberCards.isNotEmpty() -> {
                                // discard first valid set
                                kotlinClientInterface.discardCard(discardableNumberCards[0])
                            }

                            else -> {
                                // check whether this client took a card from the discard pile in the previous action
                                if (game.state == GameState.CARD_DRAWN) {
                                    // this client drew a card in the previous action, say nope, because the client can not draw any card
                                    kotlinClientInterface.sayNope()
                                } else {
                                    // this client did not draw a card last round and can not discard any set of cards
                                    kotlinClientInterface.takeCard()
                                }
                            }
                        }
                    }
                }

                GameState.GAME_END -> {
                    log.fine("game state GAME_END")
                    // game ended

                }

                GameState.CANCELLED -> {
                    log.fine("game state CANCELLED")
                    // cancelled

                }
            }
        }
    }

    /**
     * Handles the state, that the discard pile top card is of type nominate and the client player should discard a
     * specific amount of cards with a specific card color, defined by the [Game.lastNominateColor] for "multi" nominate
     * cards and [Card.colors] for non "multi" cards
     * */
    private fun handleNominateCard(game: Game, currentDiscardPileCard: Card, clientPlayer: Player) {
        // if this nominate card is a "multi" nominate card (all colors)
        val colors = if (currentDiscardPileCard.hasAllColors()) {
            listOf(game.lastNominateColor)
        } else {
            currentDiscardPileCard.colors
        }
        val discardableNumberCards =
            gameLogic.findDiscardableNumberCardSet(colors, game.lastNominateAmount, clientPlayer.cards)
        val discardableActionCards = gameLogic.getDiscardableActionCards(colors, clientPlayer.cards)
        if (discardableNumberCards.isNotEmpty()) {
            // discard matching number card for the condition of the nominate card
            kotlinClientInterface.discardCard(discardableNumberCards[0])
            log.info("discarded number card after nominate")
        } else if (discardableActionCards.isNotEmpty()){
            // discard matching action card for the condition of the nominate card
            discardBestActionCard(discardableActionCards, game, clientPlayer)
            log.info("discarded action card after nominate")
        } else if (game.state == GameState.TURN_START) {
            // take card after nominate card
            kotlinClientInterface.takeCard()
            log.info("draw card after nominate")
        } else {
            // say nope after nominate card
            kotlinClientInterface.sayNope()
            log.info("said nope after nominate")
        }
    }

    /**
     * Determines the best action card to be discarded and discards this action card
     * */
    private fun discardBestActionCard(
        discardableActionCards: List<Card>,
        game: Game,
        clientPlayer: Player
    ) {
        // TODO implement logic, that discards specific action card
        when (discardableActionCards[0].type) {
            CardType.NOMINATE -> {
                val canChooseColor = game.discardPile[0].hasAllColors()
                // test call use nominate card
                kotlinClientInterface.nominateCard(
                    cards = listOf(discardableActionCards[0]),
                    nominatedPlayer = gameLogic.determineNominatedPlayer(game, clientPlayer),
                    // TODO only send color when canChooseColor. The sever currently ignores this to be set
                    nominatedColor = gameLogic.determineNominatedColor(game, clientPlayer),
                    nominatedAmount = gameLogic.determineNominatedAmount(game, clientPlayer)
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
        println("Game end (gameId: ${game.id}). Result ranking:")
        println(playerListResult)

        if (Config.ASK_TO_INVITE_AFTER_GAME_FINISHED) {
            runBlocking {
                launch {
                    askToInviteUsers()
                }
            }
        }
    }

    override fun tournamentEnd(tournament: Tournament) {
        log.fine("tournamentEnd received (tournamentId: ${tournament.id}, participants: ${tournament.participants})")

        // print all players sorted by their ranking
        val playerListResult =
            tournament.participants.sortedBy { it.ranking }
                .joinToString(separator = "\n") { it.getEndGameStringFormat() }
        println("Tournament end (tournamentId: ${tournament.id}). Result ranking:")
        println(playerListResult)
    }

    override fun gameInvite(game: Game): Boolean {
        log.fine("gameInvite received")
        println("Game invitation received (gameId: ${game.id})")

        // automatically accept if defined in config
        if (Config.ACCEPT_INVITATION_AUTOMATICALLY) return true

        return readBool(
            "Game invitation received (GameID: ${game.id}). " +
                    "Accept y/n (default ${Config.ACCEPT_GAME_INVITATION_DEFAULT.toConsoleStringRepresentation()}): "
        ) ?: Config.ACCEPT_GAME_INVITATION_DEFAULT
    }

    override fun tournamentInvite(tournament: Tournament): Boolean {
        log.fine("tournamentInvite invoked(tournament: $tournament)")
        println("Tournament invitation received (tournamentId: ${tournament.id})")

        // automatically accept if defined in config
        if (Config.ACCEPT_INVITATION_AUTOMATICALLY) return true

        return readBool(
            "Tournament invitation received (TournamentID: ${tournament.id}). " +
                    "Accept y/n (default ${Config.ACCEPT_TOURNAMENT_INVITATION_DEFAULT.toConsoleStringRepresentation()}): "
        ) ?: Config.ACCEPT_TOURNAMENT_INVITATION_DEFAULT
    }
}