package rest

import Constants
import entity.Player
import entity.StartGamePostData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import java.util.logging.Logger

/**
 * Class to signup and signin to the nope card game server via the REST api
 *
 * Authentication:
 * - SignUp/SignIn -> uses username, password string to signup/signin the client
 * >>>> these requests return a [LoginReturnData] object, which is used to authenticate from now on using
 *
 * - all other requests -> uses [LoginReturnData] object, which internally contains the token
 */
class RESTApi {
    private val log = Logger.getLogger(this.javaClass.name)

    private var client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Signs up a new client account. The server should return a JSON WebToken on success.
     * @param username username for the new account
     * @param password password for the new account
     *
     * @return the response from the server
     * */
    suspend fun signUp(username: String, password: String): LoginReturnData {
        val loginReturnData: LoginReturnData = client.post(Constants.API.SIGNUP) {
            contentType(ContentType.Application.Json)
            setBody(LoginCredentials(username = username, password = password))
        }.body()

        authenticateClient(loginReturnData)
        return loginReturnData
    }

    /**
     * Signs in a new client. The server should return a JSON WebToken on success.
     * @param username username for the account
     * @param password password for the account
     *
     * @return the response from the server
     * */
    suspend fun signIn(username: String, password: String): LoginReturnData {
        val loginReturnData: LoginReturnData = client.post(Constants.API.SIGNIN) {
            authenticate(username = username, password = password)
        }.body()

        authenticateClient(loginReturnData)
        return loginReturnData
    }

    /**
     * Starts a nope game
     * */
    suspend fun startGame(gameConfig: StartGamePostData): StartGamePostData? {
        return client.post(Constants.API.START_GAME){
            contentType(ContentType.Application.Json)
            setBody(gameConfig)
        }.body()
    }

    /**
     * Lists all user connections
     * */
    suspend fun userConnections(): List<Player> {
        return client.get(Constants.API.USER_CONNECTIONS).body()
    }

    /**
     * Authenticates the internal http client to use the given token as bearer
     *
     * @param loginReturnData session data that should be used as authentication
     * */
    private fun authenticateClient(loginReturnData: LoginReturnData) {
        client = client.config {
            install(Auth) {
                bearer {
                    loadTokens {
                        // set bearer token, refresh token is not defined per project documentation
                        BearerTokens(loginReturnData.jsonWebToken, "")
                    }
                }
            }
        }
    }

    /**
     * Authenticates the request by setting [LoginCredentials] as post body
     * */
    private fun HttpRequestBuilder.authenticate(username: String, password: String) {
        contentType(ContentType.Application.Json)
        setBody(LoginCredentials(username = username, password = password))
    }
}