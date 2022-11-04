package RiotGamesDiscordBot.RiotGamesAPI.Containers;

import com.google.gson.annotations.SerializedName;

public class MiniSeries {
    @SerializedName("losses")
    int losses;

    @SerializedName("progress")
    String progress;

    @SerializedName("target")
    int target;

    @SerializedName("wins")
    String wins;
}
