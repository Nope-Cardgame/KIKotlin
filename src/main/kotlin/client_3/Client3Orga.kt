package client_3
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
        log.fine("gameStateUpdate received")
        //check if its this clients turn
        if (game.currentPlayer.username == username) {

           when(game.state) {

               GameState.GAME_START -> TODO()
               GameState.NOMINATE_FLIPPED -> {}//not going to happen

               GameState.TURN_START -> {
                   //checks if the first card is invisible-special to know on wich card to look at
                   val relevantBoardCard: Card = if (game.discardPile[0].type == CardType.INVISIBLE) {
                        game.discardPile[1]
                   } else {
                       game.discardPile[0]
                   }
                   //checks if there is a discard able set in hand, matching color and amount
                   var discard: List<Card> = logic.checkForDiscard(game.currentPlayer.cards,relevantBoardCard)
                   println("---------to discard-------------")
                   for(card in discard) {
                       println(card.name)
                   }

                   //take a card
                   if (discard.isEmpty()) {
                       kotlinClientInterface.takeCard()
                       println("No set to discard, i get a card!")
                   } else {
                       //checks for special cards in hand and if they are playable
                       var gotSpecial: Boolean = false
                       for(card in game.currentPlayer.cards) {
                           if (card.type != CardType.NUMBER) {
                               if (card.type == CardType.RESET) {
                                   gotSpecial = true
                                   discard = List<Card>(1) { card }
                               }
                               for (color in relevantBoardCard.colors) {
                                   if (card.colors[0] == color) {

                                       discard = List<Card>(1) { card }
                                       break
                                   }
                               }
                           }
                       }
                       //discards the chosen card('s)
                       if (gotSpecial) {
                           if (discard[0].type == CardType.NOMINATE) {
                               kotlinClientInterface.nominateCard(discard,game.players[1],CardColor.RED,2,"random :)")
                           }
                           kotlinClientInterface.discardCard(discard,"i got a special card!")
                       } else {
                           kotlinClientInterface.discardCard(discard,"first set to be found")
                       }

                   }

               }

               GameState.CARD_DRAWN -> {
                   //checks if the first card is invisible-special to know on wich card to look at
                   val relevantBoardCard: Card = if (game.discardPile[0].type == CardType.INVISIBLE) {
                       game.discardPile[1]
                   } else {
                       game.discardPile[0]
                   }
                   //checks if there is a discard able set in hand, matching color and amount
                   var discard: List<Card> = logic.checkForDiscard(game.currentPlayer.cards,relevantBoardCard)
                   println("---------to discard-------------")
                   for(card in discard) {
                       println(card.name)
                   }


                   if (discard.isEmpty()) {
                       println("NOPE! Still no set to discard :)")
                       kotlinClientInterface.sayNope()
                   } else {
                       kotlinClientInterface.discardCard(discard,"first set to be found")
                   }

               }
               GameState.CANCELLED -> TODO()
               GameState.GAME_END -> TODO()
           }
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