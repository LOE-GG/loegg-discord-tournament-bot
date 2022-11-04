package RiotGamesDiscordBot.Tournament.RoundRobin.Events;

import RiotGamesDiscordBot.EventHandling.ErrorEvent;
import RiotGamesDiscordBot.Tournament.Team;
import RiotGamesDiscordBot.Tournament.Tournament;

public class DuplicateOpponentErrorEvent extends ErrorEvent {

    public DuplicateOpponentErrorEvent(Team team) {
        super("DuplicateOpponent Exception");
        this.message = "Team [ " + team.getTeamName() + " ] was placed against another team twice. A possible reason for this is " +
                "two teams in the tournament having the same name. If this is not the case, please contact [ INSERT CONTACT INFO HERE ] " +
                "with the tournament config file so this can be resolved as soon as possible.";
    }

    @Override
    public void setup(Tournament tournament) {
        System.out.println("Setting up DuplicateOpponentEvent");
        tournament.setCannotContinue(true);
    }
}
