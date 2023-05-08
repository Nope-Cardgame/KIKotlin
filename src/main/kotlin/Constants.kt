/**
 * Organizes the string constants for the api and the connection to the server
 */
object Constants {
    private const val HOST = "http://nope.ddns.net"

    object API {
        private const val API_PATH = "$HOST/api"

        const val SIGNUP = "$API_PATH/signup"
        const val SIGNIN = "$API_PATH/signin"
        const val START_GAME = "$API_PATH/game"
        const val USER_CONNECTIONS = "$API_PATH/userConnections"
    }

    object WebSocket {
        const val URL = HOST
        const val AUTH_TOKEN_KEY = "token"

        object EVENTS {
            const val CONNECT = "connect"
            const val CONNECT_ERROR = "connect_error"
            const val DISCONNECT = "disconnect"

            // individual nope related game events
            const val GAME_STATE = "gameState"
            const val ERROR = "error"
            const val BANNED = "banned"
            const val PLAY_ACTION = "playAction"
            const val GAME_END = "gameEnd"
            const val TOURNAMENT_INVITE = "tournamentInvite"
            const val GAME_INVITE = "gameInvite"
            const val READY = "ready"
        }
    }
}