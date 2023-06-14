package client3
import KotlinClientInterface
import LogConsoleFormatter
import NopeEventListener
import entity.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
/**
 * Client3Orga-NopeClient
 *
 * In this class the socket.io calls get answered, the Nope game structure is build
 * and the logic is outsourced to [Client3Logic].
 *
 * @param username: username of this client
 * @param password: password of this client
 * @param usernameToInvite: username of the client to invite, used for test-runs
 *
 * @author [Jan Rasche](https://github.com/Muquinbla)
 */
class Client3Orga(private val username: String, password: String, private val usernameToInvite: String? = null
                    ) : NopeEventListener {
    private val log: Logger
    private val logic = Client3Logic()
    private val kotlinClientInterface: KotlinClientInterface
    private var invitedUser: Boolean = false
    private var currGameID: String =""


/**
 * initiates the logger and loggs the client onto the server
* */
    init {
        log = Logger.getLogger("${javaClass.name}/$username")
        kotlinClientInterface = KotlinClientInterface(username, password, this)

        // setup logger
        val consoleHandler = FileHandler("./log_$username.txt")
        consoleHandler.level = Level.ALL
        consoleHandler.formatter = LogConsoleFormatter()
        log.addHandler(consoleHandler)
        log.level = Level.ALL
        log.useParentHandlers = false
    }

    /**
     * funktion to test a game with 2 (of this) clients
     * */
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


    /**** Overridden Websocket Events ****/
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
        //check if its this clients turn
        if (game.currentPlayer.username == username) {

           when(game.state) {

               GameState.GAME_START -> {
               }
               GameState.NOMINATE_FLIPPED -> { //game start with nominated flipped
                   val col = game.discardPile[0].colors[0]
                   if (game.discardPile[0].colors.size > 1) {
                       kotlinClientInterface.nominateCard(
                           listOf(),
                           nextPlayer,
                           col,
                           2,
                           "Nominated played"
                       )
                   } else {
                       kotlinClientInterface.nominateCard(
                           listOf(),
                           nextPlayer,
                           null,
                           2,
                           "Nominated played"
                       )
                   }

               }

               GameState.TURN_START, GameState.CARD_DRAWN -> {
                   // log print at game start, with current game info
                   if (currGameID != game.id) {
                       currGameID = game.id
                       println("Game has started")
                       log.fine("\n")
                       log.fine("Game started at ${game.startTime} with the game ID: ${game.id}\n" +
                               "Game started with: \n" +
                               "Action Cards: ${!game.noActionCards}\n" +
                               "Wild Cards: ${!game.noWildCards}\n" +
                               "1 more Start card: ${game.oneMoreStartCards}\n" +
                               "                                     |sockedId               | disqualified         |Name ")
                       for (player in game.players) {
                           log.fine("   ${player.socketId},         ,         ${player.disqualified},         ${player.username}")
                       }
                       log.fine("\n")
                   }
                   //checks if the first card is invisible-special to know at wich card to look at
                   val relevantBoardCard: Card = logic.checkInvisible(game.discardPile)
                   //checks if there is a discard able set in hand, matching color and amount
                   val discard: List<Card> = logic.checkForDiscard(game.currentPlayer.cards,relevantBoardCard, game)

                   //if no fitting set was found
                   if (discard.isEmpty()) {
                       //if a card was drawn yet
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
               GameState.GAME_END -> {
                   log.fine("The game (${game.id}) ended at ${game.endTime}") //TODO
               }
           }
        }
    }


    /**
     * Manages the discard process and checks for special cards in hand and if they are playable (uses last special card)
     * @param game: current played game
     * @param disc: list of numberCards to discard, with fitting color
     * @param relevantBoardCard: the uppermost relevant card (ignoring invisible)
     * @param nextPlayer: the player who is next up in the game and not qualified
     * */
    private fun discardChosenOnes(game: Game, disc: List<Card>, relevantBoardCard: Card, nextPlayer: Player) {
        var discard: List<Card> = disc
        val boardCol = if (relevantBoardCard.type == CardType.NOMINATE && relevantBoardCard.colors.size > 1) {listOf(game.lastNominateColor)} else {relevantBoardCard.colors}

        for(card in game.currentPlayer.cards) {
            when (card.type) {
                //do nothing with number cards
                CardType.NUMBER -> {
                }
                // nominate card found
                CardType.NOMINATE -> {
                    for (col in boardCol) {
                        var color: CardColor = col
                        if (game.lastAction.type == GameActionType.NOMINATE) {
                            color = game.lastNominateColor
                        }
                        if (card.colors.size > 1) {
                                discard = List<Card>(1) { card }
                                break
                        } else {
                            if (card.colors[0] == col) {
                                discard = List<Card>(1) { card }
                                break
                            }
                        }
                    }
                }
                // reset card found
                CardType.RESET -> {
                    discard = List<Card>(1) { card }
                    break
                }
                // invisible card found
                CardType.INVISIBLE -> {
                    for (color in boardCol) {
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
            // makes sure only special card is discarded
            discard = discard.subList(0,1)
            if (discard[0].type == CardType.NOMINATE) {
                if(discard[0].colors.size > 1) {
                    // choose color for nominate
                    var chosenColor: CardColor  = CardColor.RED
                    var chosenColValue: Int = logic.getRemainingCards(CardColor.RED, game)

                    val green = logic.getRemainingCards(CardColor.GREEN, game)
                    if (green <= chosenColValue) {
                        chosenColValue = green
                        chosenColor = CardColor.GREEN
                    }

                    val blue = logic.getRemainingCards(CardColor.BLUE, game)
                    if (blue <= chosenColValue) {
                        chosenColValue = blue
                        chosenColor = CardColor.BLUE
                    }

                    val yellow = logic.getRemainingCards(CardColor.YELLOW, game)
                    if (yellow <= chosenColValue) {
                        chosenColor = CardColor.YELLOW
                    }
                    println("       +++multiNominate+++")
                    kotlinClientInterface.nominateCard(
                        discard,
                        nextPlayer,
                        chosenColor,
                        2,
                        "random :)"
                    )
                } else {
                    println("       +++singleNominate+++")
                    kotlinClientInterface.nominateCard(
                        discard,
                        nextPlayer,
                        null,
                        2,
                        "random :)"
                    )
                }

            } else {
                println("       +++discardSpecial+++")
                kotlinClientInterface.discardCard(discard,"i got a special card!")
            }
        } else {
            println("       +++discardNumberPack+++")
            kotlinClientInterface.discardCard(discard,"first set to be found")
        }
    }

    override fun communicationError(communicationError: CommunicationError) {
        log.fine("communicationError received: ${communicationError.message}")
        println("communicationError: ${communicationError.message}")
    }

    override fun clientEliminated(playerEliminated: PlayerEliminated) {
        log.fine("clientEliminated received, reason:${playerEliminated.reason}, is disqualified: ${playerEliminated.disqualified}")
        if (playerEliminated.disqualified) {
            println("clientEliminated, reason:${playerEliminated.reason}, is disqualified: ${playerEliminated.disqualified}")
        } else {
            println("Hand is empty, game is lost")
        }

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
        println("tournament invite received for: ${tournament.id}")
        // accept all invitations by default
        return true
    }

}