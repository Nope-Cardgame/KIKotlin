fun main() {
    val username = "kotlin3"
    val password = "kotlin3"
    val username2 = "kotlin4"
    val password2 = "kotlin4"

    // instantiate clients
    val testNopeClient2 = TestNopeClient(
        username = username2,
        password = password2
    )
    val testNopeClient1 = TestNopeClient(
        username = username,
        password = password,
        usernameToInvite = username2
    )

}