package RiotGamesDiscordBot.RiotGamesAPI;

import com.google.gson.annotations.SerializedName;

public class RiotAPIStatusLine {
    @SerializedName("message")
    String message;

    @SerializedName("status_code")
    int statusCode;


    @Override
    public String toString() {
        return this.statusCode + " " + this.message;
    }

}
