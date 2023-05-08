import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import java.util.logging.Logger

/**
 * Class to signup and signin to the nope card game server via the REST api
 */
class RESTApi {
    private val log = Logger.getLogger(this.javaClass.name)

    private val client: HttpClient = HttpClient(CIO) {
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
        val response: HttpResponse = client.post(Constants.API.SIGNUP) {
            contentType(ContentType.Application.Json)
            setBody(LoginCredentials(username = username, password = password))
        }
        log.finer("signUp http request finished")

        return response.body()
    }

    /**
     * Signs in a new client. The server should return a JSON WebToken on success.
     * @param username username for the account
     * @param password password for the account
     *
     * @return the response from the server
     * */
    suspend fun signIn(username: String, password: String): LoginReturnData {
        val response: HttpResponse = client.post(Constants.API.SIGNIN) {
            contentType(ContentType.Application.Json)
            setBody(LoginCredentials(username = username, password = password))
        }
        log.finer("signIn http request finished")

        return response.body()
    }
}