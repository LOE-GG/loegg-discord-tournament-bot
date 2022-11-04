package RiotGamesDiscordBot.RiotGamesAPI.Event;

import RiotGamesDiscordBot.EventHandling.ErrorEvent;
import RiotGamesDiscordBot.Tournament.Tournament;

public class SummonerNotFoundErrorEvent extends ErrorEvent {

    public SummonerNotFoundErrorEvent(String summonerName) {
        super("SummonerNotFound Exception");
        this.message = summonerName + " was not found. Common reasons for this can include a " +
                "misspelled Summoner Name or this Summoner is not in the NA region.\n" +
                "Please correct the spelling or replace them in the tournament configuration file and restart " +
                "the tournament.";
    }

    @Override
    public void setup(Tournament tournament) {
        tournament.setCannotContinue(true);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
