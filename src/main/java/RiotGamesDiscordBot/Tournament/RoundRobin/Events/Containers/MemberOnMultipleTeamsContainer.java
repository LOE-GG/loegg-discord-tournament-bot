package RiotGamesDiscordBot.Tournament.RoundRobin.Events.Containers;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.Tournament.Team;

import java.util.ArrayList;
import java.util.List;

public class MemberOnMultipleTeamsContainer {

    public List<Team> teams;
    public SummonerInfo summonerInfo;

    public MemberOnMultipleTeamsContainer(SummonerInfo summonerInfo) {
        this.summonerInfo = summonerInfo;
        this.teams = new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SummonerInfo) {
            return object.equals(this.summonerInfo);
        }
        else if (object instanceof MemberOnMultipleTeamsContainer) {
            return ((MemberOnMultipleTeamsContainer) object).summonerInfo.equals(this.summonerInfo);
        }

        else return false;
    }
}
