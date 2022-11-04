package RiotGamesDiscordBot.Tournament.SingleElimination;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.Tournament.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.Array;
import java.util.*;

public class SingleEliminationTournament extends Tournament {

    private final SingleEliminationBracketManager bracketManager;

    public SingleEliminationTournament(TournamentConfig tournamentConfig, GuildMessageReceivedEvent event, Map<String, List<String>> teams, DiscordUser creator) {
        super(tournamentConfig, event, teams, creator);
        this.bracketManager = new SingleEliminationBracketManager();
    }

    @Override
    public void setup() {
        this.setUpTeams();

        //If there are an odd number of teams
        if ((this.teams.size() % 2) == 1) {
            this.teams.add(new Team("BYE"));
        }

        List<Match> matches = new ArrayList<>();

        Random randomNumGen = new Random();
        List<Integer> chosenTeamPos = new ArrayList<>();
        int teamsSize = this.teams.size();
        System.out.println("Teams size: " + this.teams.size());
        while (chosenTeamPos.size() < teamsSize) {
            System.out.println("unassignedTeams: " + teamsSize);
            int teamOnePos = randomNumGen.nextInt(teamsSize);
            while (chosenTeamPos.contains(teamOnePos)) {
                teamOnePos = randomNumGen.nextInt(teamsSize);
            }
            chosenTeamPos.add(teamOnePos);

            int teamTwoPos = randomNumGen.nextInt(teamsSize);
            while (chosenTeamPos.contains(teamTwoPos)) {
                teamTwoPos = randomNumGen.nextInt(teamsSize);
            }
            chosenTeamPos.add(teamTwoPos);
            Match match = new Match(this.teams.get(teamOnePos), this.teams.get(teamTwoPos));
            matches.add(match);
        }

        Round roundOne = new Round(matches, 1);
        System.out.println(roundOne);

    }

    @Override
    public void start() {

    }

    @Override
    public void advanceTournament(MatchResult matchResult) {

    }

    @Override
    public void endTournament() {

    }

    @Override
    public void idle() {

    }

    @Override
    public void resume() {

    }
}
