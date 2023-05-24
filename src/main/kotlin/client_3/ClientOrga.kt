package client_3
import KotlinClientInterface
import NopeEventListener
import entity.*
import entity.action.CardAction
import java.util.logging.Logger

class ClientOrga : NopeEventListener {
    private val username: String
    private val password: String
    private val log: Logger
    private val logic = ClientLogic()
    private val kotlinClientInterface: KotlinClientInterface


    init {
        println("Enter username here: ")
        username = readln()
        println("Enter password here: ")
        password = readln()
        log = Logger.getLogger("${javaClass.name}/$username")
        kotlinClientInterface = KotlinClientInterface(username, password, this)
    }
    override fun socketConnected() {
        log.fine("socketConnected received")
        println("$username connected")
    }

    override fun socketConnectError(error: String?) {
        log.fine("socketConnectError received")
        println("$username socket connection error: $error")
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
        if (game.currentPlayer.username == username) {
           when(game.state) {

               GameState.GAME_START -> TODO()
               GameState.NOMINATE_FLIPPED -> TODO()

               GameState.TURN_START -> {
                   val discard: List<Card> = logic.checkForDiscard(game.currentPlayer.cards,game.initialTopCard)

                   if (discard.isEmpty()) {
                       kotlinClientInterface.takeCard()
                       println("No set to discard, i get a card!")
                   }
                   kotlinClientInterface.discardCard(discard,"first set to be found")
               }

               GameState.CARD_DRAWN -> {
                   val discard: List<Card> = logic.checkForDiscard(game.currentPlayer.cards,game.initialTopCard)

                   if (discard.isEmpty()) {
                       println("NOPE! Still no set to discard :)")
                   }
                   kotlinClientInterface.discardCard(discard,"first set to be found")
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
        println("game invite accepted")
        // accept all invitations by default
        return true
    }

    override fun tournamentInvite(tournament: Tournament): Boolean {
        log.fine("tournamentInvite invoked(tournament: $tournament)")
        println("tournament invite accepted")
        // accept all invitations by default
        return true
    }

}