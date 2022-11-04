package RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MapType;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.PickType;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SpectatorType;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchMetaData;
import RiotGamesDiscordBot.Tournament.TournamentConfig;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class TournamentCodeParameters {
    private final List<String> allowedSummonerIds;
    private final double teamSize;
    private final String metadata;
    private final PickType pickType;
    private final MapType mapType;
    private final SpectatorType spectatorType;

    public TournamentCodeParameters(List<String> allowedSummonerIds, TournamentConfig tournamentConfig, MatchMetaData metaData) {
        this.teamSize = tournamentConfig.getTeamSize();
        this.pickType = tournamentConfig.getPickType();
        this.mapType = tournamentConfig.getMapType();
        this.spectatorType = tournamentConfig.getSpectatorType();
        this.metadata = new Gson().toJson(metaData);

        this.allowedSummonerIds = new ArrayList<>();
        this.allowedSummonerIds.addAll(allowedSummonerIds);
    }

    public double getTeamSize() {
        return teamSize;
    }
}
