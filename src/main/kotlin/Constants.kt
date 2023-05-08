/**
 * Organizes the string constants for the api and the connection to the server
 */
object Constants {
    private const val HOST = "http://nope.ddns.net"

    object API {
        private const val API_PATH = "$HOST/api"

        const val SIGNUP = "$API_PATH/signup"
        const val SIGNIN = "$API_PATH/signin"
    }

    object WebSocket {
        const val URL = HOST
        const val AUTH_TOKEN_KEY = "token"

        object EVENTS {
            const val CONNECT = "connect"
            const val CONNECT_ERROR = "connect_error"
            const val DISCONNECT = "disconnect"
        }
    }
}