package rest

import Constants
import entity.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*

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
    suspend fun signIn(username: String, password: String): LoginReturnData? {
        val result: HttpResponse = client.post(Constants.API.SIGNIN) {
            authenticate(username = username, password = password)
        }

        return if (result.status.isSuccess()) {
            val loginReturnData: LoginReturnData = result.body()
            authenticateClient(loginReturnData)
            loginReturnData
        } else null
    }

    /**
     * Starts a nope game
     * */
    suspend fun startGame(gameConfig: StartGamePostData): StartGamePostData {
        val cont = client.post(Constants.API.START_GAME) {
            contentType(ContentType.Application.Json)
            setBody(gameConfig)
        }
        println(cont.body<String>())
         return cont.body()
    }

    /**
     * Starts a tournament
     * */
    suspend fun startTournament(tournamentConfig: StartTournamentPostData): Tournament {
        return client.post(Constants.API.START_TOURNAMENT){
            contentType(ContentType.Application.Json)
            setBody(tournamentConfig)
        }.body()
    }

    /**
     * Returns game by id
     * */
    suspend fun getGame(gameId: String): Game {
        return client.get(Constants.API.GAME_INFO + gameId).body()
    }

    /**
     * Returns all played games
     * */
    suspend fun getGames(): List<Game> {
        return client.get(Constants.API.GAME_INFO).body()
    }

    /**
     * Returns tournament by id
     * */
    suspend fun getTournament(tournamentId: String): Tournament {
        return client.get(Constants.API.TOURNAMENT_INFO + tournamentId).body()
    }

    /**
     * Returns all played tournaments
     * */
    suspend fun getTournaments(): List<Tournament> {
        return client.get(Constants.API.TOURNAMENT_INFO).body()
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