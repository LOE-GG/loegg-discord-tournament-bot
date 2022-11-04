package RiotGamesDiscordBot.Tournament.RoundRobin.Events;

import RiotGamesDiscordBot.EventHandling.ErrorEvent;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.Tournament.Team;
import RiotGamesDiscordBot.Tournament.Tournament;

public class TeamMemberDuplicateErrorEvent extends ErrorEvent {

    public TeamMemberDuplicateErrorEvent(SummonerInfo duplicateMember, Team team) {
        super("TeamMemberDuplicate Exception");
        this.message = duplicateMember.getSummonerName() + " seems to be on " + team.getTeamName() +
                " more than once.\n\n" + " Please ensure that each team does not have duplicate members in the Tournament Config file and then re-attempt tournament creation.";
    }

    @Override
    public void setup(Tournament tournament) {

    }
}
