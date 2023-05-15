import client1.Client1

fun main() {
    val username = "kotlin"
    val password = "kotlin"
    val username2 = "kotlin2"
    val password2 = "kotlin2"

    // instantiate clients
    val client2 = Client1(
        username = username2,
        password = password2
    )
    val client1 = Client1(
        username = username,
        password = password,
        usernameToInvite = username2
    )

}