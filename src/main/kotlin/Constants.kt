/**
 * Organizes the string constants for the api and the connection to the server
 */
object Constants {
    object API {
        private const val HOST = "http://nope.ddns.net"
        private const val API_PATH = "$HOST/api"

        const val SIGNUP = "$API_PATH/signup"
        const val SIGNIN = "$API_PATH/signin"
    }
}