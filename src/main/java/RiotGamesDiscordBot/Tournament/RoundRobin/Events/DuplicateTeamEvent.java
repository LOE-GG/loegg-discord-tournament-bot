package RiotGamesDiscordBot.Tournament.RoundRobin.Events;

import RiotGamesDiscordBot.EventHandling.ErrorEvent;
import RiotGamesDiscordBot.Tournament.Team;
import RiotGamesDiscordBot.Tournament.Tournament;

public class DuplicateTeamEvent extends ErrorEvent {

    public DuplicateTeamEvent(Team duplicateTeam) {
        super("DuplicateTeam Exception");
        this.message = duplicateTeam.getTeamName() + " " +
                "is listed in the Tournament Configuration excel file multiple times.\n\n" +
                "There is no Bot Command that can rectify this event. Because of this, the tournament will be removed. " +
                "Please remove or replace the duplicate team and try again.";
    }

    @Override
    public void setup(Tournament tournament) {

    }
}
