import client_3.Client3Orga

fun main() {
    val username = "kotlin"
    val password = "kotlin"
    val username2 = "kotlin2"
    val password2 = "kotlin2"

    // instantiate clients
//    val testNopeClient2 = TestNopeClient(
//        username = username2,
//        password = password2
//    )
//    val testNopeClient1 = TestNopeClient(
//        username = username,
//        password = password,
//        usernameToInvite = username2
//    )

    val Client3Orga = Client3Orga(
        username = username2,
        password = password2
    )

    val client3Orga = Client3Orga(
        username = username,
        password = password,
        usernameToInvite = username2
    )

}
