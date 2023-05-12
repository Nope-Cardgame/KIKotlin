import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val username = "kotlin"
    val password = "kotlin"
    val username2 = "kotlin2"
    val password2 = "kotlin2"

    // instantiate client
    val testNopeClient1 = TestNopeClient(
        username = username,
        password = password
    )
    val testNopeClient2 = TestNopeClient(
        username = username2,
        password = password2
    )

}