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
 *
 * TODO add explanation parameter to all game actions
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
            const val ASK_TO_INVITE_ON_CONNECT = false
            const val ASK_TO_INVITE_AFTER_FINISHED = false
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
                const val TOURNAMENT_NUMBER_OF_ROUNDS = 15
                const val TOURNAMENT_POINTS_PER_GAME_WIN = true
            }

            object Console {
                const val BOOL_TRUE = "y"
                const val BOOL_FALSE = "n"
                const val SPLIT_DELIMITER = ","
            }

        }

        object Explanation {
            const val DISCARD_RESET_CARD = "Neusart Karte gespielt, da der nächste Spieler nur noch eine Karte hat und das Spiel dadurch beendet werden würde, oder keine bessere Aktionskarte gefunden wurde"
            const val DISCARD_INVISIBLE_CARD = "Durchblickkarte gespielt, da keine bessere Aktions- oder Zahlenkarte gefunden wurde"
            const val DISCARD_NOMINATE_CARD = "Auswahlkarte auf den Spieler mit den wenigste Karten ausgespielt, da diese als beste spielbare Aktionskarte angesehen wurde"
            const val SAY_NOPE_ACTION_ON_NOMINATED = "Nope! gesagt, nachdem eine Auswahlkarte auf diesen Spieler gespielt wurde und weder die zuvor vom Nachziehstapel gezogen Karte noch irgendeine Handkarte gespielt werden kann"
            const val DRAW_CARD_ACTION_ON_NOMINATED = "Karte vom Nachziehstapel gezogen, nachdem eine Auswahlkarte auf diesen Spieler gespielt wurde und keine spielbare Handkarte gefunden wurde"
            const val NUMBER_CARD_PLAYED_ON_NOMINATED = "Zahlenkarte gespielt, die nach dem internen Evaluator am bestengeeignet war, nachdem eine Auswahlkarte auf diesen Spieler gespielt wurde"
            const val DRAW_CARD_ACTION = "Karte vom Nachziehstapel gezogen, da der Zug begonnen hat und keine spielbare Handkarte gefunden wurde"
            const val SAY_NOPE_ACTION = "Nope! gesagt, da weder die zuvor vom Nachziehstapel gezogen Karte noch irgendeine Handkarte gespielt werden kann"
            const val NUMBER_CARD_PLAYED = "Zahlenkarte gespielt, die nach dem internen Evaluator am bestengeeignet war (abhängig von der Anzahl der Farben und den passenden Handkarten)"
            const val NOMINATE_FLIPPED_ACTION = "Auswahlkarte auf den Spieler mit den wenigste Karten ausgespielt, da diese als erste Karte am Anfang des Spiels ausgelegt wurde"
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
                    val nominatedPlayer = gameLogic.determineNominatedPlayer(game, clientPlayer)
                    kotlinClientInterface.nominateCard(
                        cards = emptyList(),
                        nominatedPlayer = nominatedPlayer,
                        nominatedColor = gameLogic.determineNominatedColor(game, clientPlayer)
                            .takeIf { canChooseColor },
                        nominatedAmount = gameLogic.determineNominatedAmount(game, clientPlayer, nominatedPlayer),
                        explanation = Explanation.NOMINATE_FLIPPED_ACTION
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
                        val discardableNumberCardSet =
                            gameLogic.getDiscardableNumberCards(discardPile, clientPlayer.cards)
                        val discardableActionCards =
                            gameLogic.getDiscardableActionCards(currentDiscardPileCard.colors, clientPlayer.cards)

                        when {
                            // check whether this client can discard any action card
                            discardableActionCards.isNotEmpty() -> {
                                discardBestActionCard(discardableActionCards, game, clientPlayer)
                            }
                            // check whether this client can discard any set of cards
                            discardableNumberCardSet.isNotEmpty() -> {
                                // first card of the parameter list given to discardCard will be discarded at the top
                                kotlinClientInterface.discardCard(
                                    cards = discardableNumberCardSet,
                                    explanation = Explanation.NUMBER_CARD_PLAYED
                                )
                            }

                            else -> {
                                // check whether this client took a card from the discard pile in the previous action
                                if (game.state == GameState.CARD_DRAWN) {
                                    // this client drew a card in the previous action, say nope, because the client can not draw any card
                                    kotlinClientInterface.sayNope(Explanation.SAY_NOPE_ACTION)
                                } else {
                                    // this client did not draw a card last round and can not discard any set of cards
                                    kotlinClientInterface.takeCard(Explanation.DRAW_CARD_ACTION)
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
        val discardableNumberCardSet =
            gameLogic.findDiscardableNumberCardSet(colors, game.lastNominateAmount, clientPlayer.cards)
        val discardableActionCards = gameLogic.getDiscardableActionCards(colors, clientPlayer.cards)
        if (discardableNumberCardSet.isNotEmpty()) {
            // discard matching number card for the condition of the nominate card
            kotlinClientInterface.discardCard(
                cards = discardableNumberCardSet,
                explanation = Explanation.NUMBER_CARD_PLAYED_ON_NOMINATED
            )
            log.info("discarded number card after nominate")
        } else if (discardableActionCards.isNotEmpty()) {
            // discard matching action card for the condition of the nominate card
            discardBestActionCard(discardableActionCards, game, clientPlayer)
            log.info("discarded action card after nominate")
        } else if (game.state == GameState.TURN_START) {
            // take card after nominate card
            kotlinClientInterface.takeCard(explanation = Explanation.DRAW_CARD_ACTION_ON_NOMINATED)
            log.info("draw card after nominate")
        } else {
            // say nope after nominate card
            kotlinClientInterface.sayNope(Explanation.SAY_NOPE_ACTION_ON_NOMINATED)
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
        val bestActionCard = gameLogic.findBestActionCardToDiscard(discardableActionCards, clientPlayer, game)
        when (bestActionCard.type) {
            CardType.NOMINATE -> {
                val canChooseColor = bestActionCard.hasAllColors()
                val nominatedPlayer = gameLogic.determineNominatedPlayer(game, clientPlayer)
                // test call use nominate card
                kotlinClientInterface.nominateCard(
                    cards = listOf(bestActionCard),
                    nominatedPlayer = nominatedPlayer,
                    nominatedColor = gameLogic.determineNominatedColor(game, clientPlayer).takeIf { canChooseColor },
                    nominatedAmount = gameLogic.determineNominatedAmount(game, clientPlayer, nominatedPlayer),
                    explanation = Explanation.DISCARD_NOMINATE_CARD
                )
                log.info(loginCredentials.username + " discarded action card: " + bestActionCard)
            }

            CardType.RESET, CardType.INVISIBLE -> {
                // test call use other actions cards
                kotlinClientInterface.discardCard(
                    cards = listOf(bestActionCard),
                    explanation = if (bestActionCard.type == CardType.RESET) Explanation.DISCARD_RESET_CARD else Explanation.DISCARD_INVISIBLE_CARD
                )
                log.info(loginCredentials.username + " discarded action card: " + bestActionCard)
            }

            else -> {
                throw IllegalStateException("Card type ${bestActionCard.type} is not implemented for element in discardableActionCards")
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

        if (!Config.START_TOURNAMENT && Config.ASK_TO_INVITE_AFTER_FINISHED) {
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

        if (Config.START_TOURNAMENT && Config.ASK_TO_INVITE_AFTER_FINISHED) {
            runBlocking {
                launch {
                    askToInviteUsers()
                }
            }
        }
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