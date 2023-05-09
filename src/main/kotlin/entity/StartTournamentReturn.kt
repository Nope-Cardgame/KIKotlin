package entity

import com.google.gson.annotations.SerializedName


/**
 * Represents the result returned by the start game api endpoint [Constants.API.START_TOURNAMENT]
 */
data class StartTournamentReturn(
    @SerializedName("modus")
    val mode: TournamentMode,
    val players: List<Player>
)