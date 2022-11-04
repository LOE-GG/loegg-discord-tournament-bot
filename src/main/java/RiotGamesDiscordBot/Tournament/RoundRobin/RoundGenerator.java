package RiotGamesDiscordBot.Tournament.RoundRobin;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchMetaData;
import RiotGamesDiscordBot.Tournament.Match;
import RiotGamesDiscordBot.Tournament.Round;
import RiotGamesDiscordBot.Tournament.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoundGenerator {
    private final List<Team> topRow;
    private final List<Team> bottomRow;

    public RoundGenerator(List<Team> teams) {
        this.topRow = new ArrayList<>();
        this.bottomRow = new ArrayList<>();

        int lastItem = teams.size() - 1;
        for (int i = 0; i < (teams.size() / 2); i++) {
            this.topRow.add(teams.get(i));
            this.bottomRow.add(teams.get(lastItem - i));
        }

    }

    public Round generateRound(int roundNum, long tournamentId) {
        List<Match> matches = new ArrayList<>();

        //Iterate through each pair of teams to create a match
        Logger.log("Creating Matches for round " + roundNum, Level.INFO);
        for (int i = 0; i < this.topRow.size() && i < this.bottomRow.size(); i++) {
            Team teamOne = this.topRow.get(i);
            Team teamTwo = this.bottomRow.get(i);

            MatchMetaData metaData = new MatchMetaData(tournamentId, UUID.randomUUID().toString());

            Match match = new Match(teamOne, teamTwo);
            match.setMetaData(metaData);
            matches.add(match);

            Logger.log(match + "\n\n" + match.getMatchMetaData().getTournamentId() + " : " + match.getMatchMetaData().getMatchId(), Level.INFO);
        }


        Round round = new Round(matches, roundNum);
        Logger.log("Successfully created round " + round.getRoundNum(), Level.INFO);
        return round;
    }

    /**
     * <pre>
     * Rotates the teams around the box counter-clockwise.
     *      1. The Team at the end of topRow is moved to the end of bottomRow.
     *      2. The Team at the beginning of bottomRow is moved to position 1 of topRow
     *
     *      -----------------
     *      | 1 | 2 | 3 | 4 |   topRow
     *      | 5 | 6 | 7 | 8 |   bottomRow
     *      -----------------
     *
     *      Team 4 would move to the position of Team 8
     *      Team 5 would move to the position of Team 2
     *
     * The movement of these teams would shift all teams one position left or right respectively.
     * </pre>
     */
    public void rotate() {
        //Remove teams from their positions.
        Team topTeam = this.topRow.remove(this.topRow.size() - 1);
        Team bottomTeam = this.bottomRow.remove(0);

        //Add teams to their final positions. topRow gets shifted to the right, bottomRow gets shifted to the left;
        this.topRow.add(1, bottomTeam);
        this.bottomRow.add(topTeam);
    }
}
