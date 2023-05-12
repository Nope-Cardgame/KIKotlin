fun main() {
    val username = "kotlin5"
    val password = "kotlin5"
    val username2 = "kotlin6"
    val password2 = "kotlin6"

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