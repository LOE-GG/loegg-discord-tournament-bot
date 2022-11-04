package SimulateTournament.Containers;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MetaData;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.Tournament.Match;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MatchComplete {
    public long startTime;

    public List<SummonerName> winningTeam;
    public List<SummonerName> losingTeam;

    public String shortCode;
    public MetaData metaData;
    public long gameId;
    public String gameName;
    public String gameType;
    public int gameMap;
    public String gameMode;
    public String region;

    public static MatchComplete fromMatch(Match match) {
        MatchComplete matchComplete = new MatchComplete();

        matchComplete.startTime = 0;
        matchComplete.winningTeam = new ArrayList<>();

        for (SummonerInfo summonerInfo : match.getTeamOne()) {
            SummonerName summonerName = new SummonerName(summonerInfo.getSummonerName());
            matchComplete.winningTeam.add(summonerName);
        }

        matchComplete.losingTeam = new ArrayList<>();
        for (SummonerInfo summonerInfo : match.getTeamTwo()) {
            SummonerName summonerName = new SummonerName(summonerInfo.getSummonerName());
            matchComplete.losingTeam.add(summonerName);
        }

        matchComplete.shortCode = "NA1234a-1a23b456-a1b2-1abc-ab12-1234567890ab";
        matchComplete.metaData = new MetaData();
        matchComplete.metaData.title = new Gson().toJson(match.getMatchMetaData());
        matchComplete.gameId = 1234567890;
        matchComplete.gameName = "a123bc45-ab1c-1a23-ab12-12345a67b89c";
        matchComplete.gameType = "PRACTICE";
        matchComplete.gameMap = 11;
        matchComplete.gameMode = "CLASSIC";
        matchComplete.region = "NA1";

        return matchComplete;

    }
}

