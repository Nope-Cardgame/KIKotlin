import entity.*

/**
 * Interface that can be implemented by the client to receive nope events
 */
interface NopeEventListener {

    /**
     * Notifies, that the client is successfully connected to the server socket
     * */
    fun socketConnected()

    /**
     * Notifies, that the client encountered a connect-error while connection to the server socket
     * */
    fun socketConnectError(error: String?)

    /**
     * Notifies, that the socket connection is disconnected from the server socket
     * */
    fun socketDisconnected()

    /**
     * Another client/player is disqualified and their hand cards are placed under the discard pile
     * */
    fun disqualifiedPlayer(player: Player, explanation: String)

    /**
     * A new game state is populated by the server
     * */
    fun gameStateUpdate(game: Game)

    /**
     * Called when a communication error occurred. This does will not disqualify the client.
     * */
    fun communicationError(communicationError: CommunicationError)

    /**
     * Called when this client is eliminated due to disqualification or empty hand cards
     * */
    fun clientEliminated(playerEliminated: PlayerEliminated)

    /**
     * Called when the game has ended
     * */
    fun gameEnd(game: Game)

    /**
     * Called when the tournament has ended
     * */
    fun tournamentEnd(tournament: Tournament)

    /**
     * Called when this client is invited to a nope game
     *
     * @return true if the invitation should be accepted else false
     * */
    fun gameInvite(game: Game): Boolean

    /**
     * Called when this client is invited to a nope tournament
     *
     * @return true if the invitation should be accepted else false
     * */
    fun tournamentInvite(tournament: Tournament): Boolean

}
