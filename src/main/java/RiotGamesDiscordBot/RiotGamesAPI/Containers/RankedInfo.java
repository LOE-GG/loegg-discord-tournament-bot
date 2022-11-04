package RiotGamesDiscordBot.RiotGamesAPI.Containers;

import com.google.gson.annotations.SerializedName;

public class RankedInfo {
    @SerializedName("leagueId")
    public String leagueId;

    @SerializedName("queueType")
    public String queueType;

    @SerializedName("tier")
    public String tier;

    @SerializedName("rank")
    public String rank;

    @SerializedName("leaguePoints")
    public int leaguePoints;

    @SerializedName("wins")
    public int wins;

    @SerializedName("losses")
    public int losses;

    @SerializedName("hotStreak")
    public boolean hotStreak;

    @SerializedName("veteran")
    public boolean veteran;

    @SerializedName("freshBlood")
    public boolean freshBlood;

    @SerializedName("miniSeries")
    public MiniSeries miniSeries;
}
