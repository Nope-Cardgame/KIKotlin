package rest

import com.google.gson.annotations.SerializedName

/**
 * Represents the JSON web token data object returned from the server after the signup/signin
 */
class LoginReturnData(
    @SerializedName("jsonwebtoken") val jsonWebToken: String
)