package entity

import com.google.gson.annotations.SerializedName

enum class TournamentState {
    @SerializedName("preparation")
    PREPARATION,
    @SerializedName("ongoing")
    ONGOING,
    @SerializedName("finished")
    FINISHED,
    @SerializedName("cancelled")
    CANCELLED
}
