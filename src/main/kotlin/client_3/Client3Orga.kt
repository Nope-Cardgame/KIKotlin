package client_3
import KotlinClientInterface
import LogConsoleFormatter
import NopeEventListener
import entity.*
import entity.action.GameAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

class Client3Orga(private val username: String, password: String, private val usernameToInvite: String? = null
                    ) : NopeEventListener {
//    private val username: String
//    private val password: String
    private val log: Logger
    private val logic = Client3Logic()
    private val kotlinClientInterface: KotlinClientInterface
    private var invitedUser: Boolean = false



    init {
//        println("Enter username here: ")
//        username = readln()
//        println("Enter password here: ")
//        password = readln()
        log = Logger.getLogger("${javaClass.name}/$username")
        kotlinClientInterface = KotlinClientInterface(username, password, this)

        // setup logger
        val consoleHandler = ConsoleHandler()
        consoleHandler.level = Level.ALL
        consoleHandler.formatter = LogConsoleFormatter()
        log.addHandler(consoleHandler)
        log.level = Level.ALL
        log.useParentHandlers = false
    }

    private suspend fun startGame() {
        val userConnections = kotlinClientInterface.getUserConnections()
        // test game with kotlin client players only
        val playerToInvite = userConnections.first { it.username == usernameToInvite }
        val clientPlayer = userConnections.first { it.username == username }

        val startGameResult = kotlinClientInterface.startGame(
            StartGamePostData(
                noActionCards = false,
                noWildCards = false,
                oneMoreStartCards = false,
                players = listOf(playerToInvite, clientPlayer)
            )
        )
        invitedUser = true
        log.fine("sent game invite to players: ${startGameResult.players}")
    }



    override fun socketConnected() {
        log.fine("socketConnected received")
        println("$username connected")

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
        println("$username socket connection error: $error")
        kotlinClientInterface.disconnectSocket()
    }

    override fun socketDisconnected() {
        log.fine("socketDisconnected received")
        println("$username disconnected")
    }

    override fun disqualifiedPlayer(player: Player, explanation: String) {
        log.fine("disqualifiedPlayer received")
        println("$player is disqualified because: $explanation")
    }

    override fun gameStateUpdate(game: Game) {
        val nextPlayer: Player = game.players.first { it.socketId != game.currentPlayer.socketId && (!it.disqualified)}
        log.fine("gameStateUpdate received")
        //check if its this clients turn
        if (game.currentPlayer.username == username) {

           when(game.state) {

               GameState.GAME_START -> {println("Game has started.")}
               GameState.NOMINATE_FLIPPED -> { //game start with nominated flipped
                   val col = game.discardPile[0].colors[0]
                   kotlinClientInterface.nominateCard(
                       listOf(),
                       nextPlayer,
                       col,
                       2,
                       "Nominated played"
                   )
               }

               GameState.TURN_START, GameState.CARD_DRAWN -> {
                   //checks if the first card is invisible-special to know on wich card to look at
                   val relevantBoardCard: Card = logic.checkInvisible(game.discardPile)
                   //checks if there is a discard able set in hand, matching color and amount
                   val discard: List<Card> = logic.checkForDiscard(game.currentPlayer.cards,relevantBoardCard, game)

                   //if no fitting set was found
                   if (discard.isEmpty()) {

                       if (game.state == GameState.CARD_DRAWN) {
                           //if the turn was a nominate
                           if (game.lastAction.type == GameActionType.NOMINATE) {
                               kotlinClientInterface.nominateCard(
                                   listOf(game.discardPile[0]),
                                   nextPlayer,
                                   game.lastNominateColor,
                                   game.lastNominateAmount,
                                   "i can't next player plz"
                               )
                               println("NOPE! Still no set to discard for this nominate :)")
                           } else {
                               println("NOPE! Still no set to discard :)")
                               kotlinClientInterface.sayNope()
                           }
                       } else {
                           kotlinClientInterface.takeCard()
                           println("No set to discard, I get a card!")
                       }

                   } else {
                       discardChosenOnes(game, discard, relevantBoardCard, nextPlayer)
                   }

               }
               GameState.CANCELLED -> {println("Game invite has been canceled..")}
               GameState.GAME_END -> TODO()
           }
        }
    }

    private fun discardChosenOnes(game: Game, disc: List<Card>, relevantBoardCard: Card, nextPlayer: Player) {
        var discard: List<Card> = disc
        val boardCol = if (relevantBoardCard.type == CardType.NOMINATE && relevantBoardCard.colors.size > 1) {listOf(game.lastNominateColor)} else {relevantBoardCard.colors}
        //checks for special cards in hand and if they are playable (uses last special card)
        for(card in game.currentPlayer.cards) {
            when (card.type) {
                CardType.NUMBER -> {
                }
                CardType.NOMINATE -> {
                    for (col in boardCol) {
                        var color: CardColor = col
                        if (game.lastAction.type == GameActionType.NOMINATE) {
                            color = game.lastNominateColor
                        }
                        if (card.colors.size > 1) {
                            if (card.colors[0] == color ||
                                card.colors[1] == color ||
                                card.colors[2] == color ||
                                card.colors[3] == color
                            ) {
                                discard = List<Card>(1) { card }
                                break
                            }
                        } else {
                            if (card.colors[0] == col) {
                                discard = List<Card>(1) { card }
                                break
                            }
                        }
                    }

                }

                CardType.RESET -> {
                    discard = List<Card>(1) { card }
                    break
                }
                CardType.INVISIBLE -> {
                    for (color in relevantBoardCard.colors) {
                        if (card.colors[0] == color) {
                            discard = List<Card>(1) { card }
                            break
                        }
                    }
                }
            }
        }
        println("---------to discard-------------")
        for(card in discard) {
            println(card.name)
        }
        //discards the chosen card('s)
        if (discard[0].type != CardType.NUMBER) {
            if (discard[0].type == CardType.NOMINATE) {
                kotlinClientInterface.nominateCard(
                    discard,
                    nextPlayer,
                    discard[0].colors[0],
                    2,
                    "random :)"
                )
            } else {
                kotlinClientInterface.discardCard(discard,"i got a special card!")
            }
        } else {
            val remove = discard.sortedByDescending {it.colors.size}// TODO

            kotlinClientInterface.discardCard(remove,"first set to be found")

        }
    }

    override fun communicationError(communicationError: CommunicationError) {
        log.fine("communicationError received: ${communicationError.message}")
        println("communicationError: ${communicationError.message}")
    }

    override fun clientEliminated(playerEliminated: PlayerEliminated) {
        log.fine("clientEliminated received, reason:${playerEliminated.reason}, is disqualified: ${playerEliminated.disqualified}")
        println("clientEliminated, reason:${playerEliminated.reason}, is disqualified: ${playerEliminated.disqualified}")
    }

    override fun gameEnd(game: Game) {
        log.fine("gameEnd received")
        println("the Game ended")

    }

    override fun tournamentEnd(tournament: Tournament) {
        log.fine("tournamentEnd received")
        println("the tournament is over")

    }
    override fun gameInvite(game: Game): Boolean {
        log.fine("gameInvite received")
        println("game invite received")
        // accept all invitations by default
        return true
    }

    override fun tournamentInvite(tournament: Tournament): Boolean {
        log.fine("tournamentInvite invoked(tournament: $tournament)")
        println("tournament invite received")
        // accept all invitations by default
        return true
    }

}