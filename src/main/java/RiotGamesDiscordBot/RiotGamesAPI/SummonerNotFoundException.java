package RiotGamesDiscordBot.RiotGamesAPI;

import java.io.IOException;

public class SummonerNotFoundException extends IOException  {
    private final String summoner;

    public SummonerNotFoundException(String summoner) {
        this.summoner = summoner;
    }

    public String getSummoner() {
        return summoner;
    }

    @Override
    public String toString() {
        return "SummonerNotFoundException : " + summoner + '\n';
    }
}
