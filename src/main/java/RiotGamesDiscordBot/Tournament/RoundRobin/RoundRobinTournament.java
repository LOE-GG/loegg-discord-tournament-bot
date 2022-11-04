package RiotGamesDiscordBot.Tournament.RoundRobin;

import RiotGamesDiscordBot.Logging.DiscordLog.DiscordTextUtils;
import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters.TournamentCodeParameters;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchMetaData;
import RiotGamesDiscordBot.RiotGamesAPI.EmbeddedMessages.TournamentWinnerEmbeddedMessageBuilder;
import RiotGamesDiscordBot.RiotGamesAPI.Event.SummonerNotFoundErrorEvent;
import RiotGamesDiscordBot.RiotGamesAPI.RiotGamesAPI;
import RiotGamesDiscordBot.RiotGamesAPI.SummonerNotFoundException;
import RiotGamesDiscordBot.Tournament.*;
import RiotGamesDiscordBot.Tournament.RoundRobin.BracketGeneration.RoundRobinBracketManager;
import RiotGamesDiscordBot.Tournament.RoundRobin.Events.DuplicateOpponentErrorEvent;
import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class RoundRobinTournament extends Tournament {
    private final List<Round> rounds = new ArrayList<>();
    private transient final RoundRobinBracketManager bracketManager;
    private int currentRound;
    private Team tournamentWinner;

    public RoundRobinTournament(TournamentConfig tournamentConfig, GuildMessageReceivedEvent event, Map<String, List<String>> teams, DiscordUser creator) {
        super(tournamentConfig, event, teams, creator);
        this.bracketManager = new RoundRobinBracketManager(this.teams, this.eventManager.getDiscordLogger().getTournamentImageChannel());
        this.currentRound = 1;
    }

    public RoundRobinTournament(TournamentConfig tournamentConfig, TextChannel textChannel, Map<String, List<String>> teams, DiscordUser creator) {
        super(tournamentConfig, textChannel, teams, creator);
        this.bracketManager = new RoundRobinBracketManager(this.teams, this.eventManager.getDiscordLogger().getTournamentImageChannel());
        this.currentRound = 1;
    }

    /**
     * Validates tournament specific data for the tournament. Validates that no team faces another twice.
     */
    @Override
    public void setup() {
        this.setUpTeams();

        // Place a fake team if there is an odd number of teams.
        if (teams.size() % 2 == 1) {
            this.teams.add(new Team("BYE"));
        }

        // Generate the Rounds
        Logger.log("Generating Rounds", Level.INFO);
        int roundNum = this.teams.size() - 1;
        RoundGenerator roundGenerator = new RoundGenerator(this.teams);
        Logger.log("Round num: " + roundNum, Level.INFO);
        Logger.log("Team size: " + this.teams.size(), Level.INFO);

        for (int i = 0; i < roundNum; i++) {
            Round round = roundGenerator.generateRound(i + 1, this.getTournamentId());
            this.rounds.add(round);
            roundGenerator.rotate();
        }

        // If for whatever reason the round generation has a team facing another twice, do not complete setup
        if (!validateRounds(this.rounds)) {
            return;
        }

        this.setSetup(true);
    }

    @Override
    public void start() {
        Logger.log("Starting RoundRobin tournament", Level.INFO);

        // Generate Bracket before creating tournament codes

        this.bracketManager.generateBracket(this.rounds);

        // Create Tournament Codes
        RiotGamesAPI riotGamesAPI = new RiotGamesAPI();
        Gson gson = new Gson();
        Logger.log("Generating Tournament Codes", Level.INFO);

        for (Round round : rounds) {
            Logger.log("Generating tournament codes for Round " + round.getRoundNum(), Level.INFO);
            for (Match match : round) {
                if (match.getTeamOne().getTeamName().equals("BYE") || match.getTeamTwo().getTeamName().equals("BYE")) {
                    continue;
                }

                Logger.log("\tGenerating tournament code for " +
                        match.getTeamOne().getTeamName() + " vs " + match.getTeamTwo().getTeamName(), Level.INFO);
                // Used to create tournament code
                List<String> summonerIds = new ArrayList<>();
                for (Team team : match) {
                    for (SummonerInfo summonerInfo : team) {
                        summonerIds.add(summonerInfo.getEncryptedSummonerId());
                    }
                }

                TournamentCodeParameters parameters = new TournamentCodeParameters(summonerIds, this.tournamentConfig, match.getMatchMetaData());
                try {
                    String response = riotGamesAPI.getTournamentCodes(this.getTournamentId(),
                            1, parameters);
                    System.out.println(response);
                    String[] tournamentCodes = gson.fromJson(response, String[].class);
                    Logger.log("\tGenerated tournament code for " +
                            match.getTeamOne().getTeamName() + " vs " + match.getTeamTwo().getTeamName(), Level.INFO);
                    match.setTournamentCode(tournamentCodes[0]);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            Logger.log("Finished generating tournament codes for Round " + round.getRoundNum(), Level.INFO);
        }

        this.bracketManager.sendRoundToChannel(this.currentRound);
        this.bracketManager.sendCurrentStandings();
        this.sendCurrentRoundMatchCodes();
    }

    private void sendCurrentRoundMatchCodes() {
        Round currentRound = this.rounds.get(this.currentRound - 1);
        for (Match match : currentRound) {
            String message = DiscordTextUtils.colorGreen("Match Code for Match " + match.getTeamOne().getTeamName() + " VS " + match.getTeamTwo().getTeamName()) + "\n" + match.getTournamentCode();
            this.creator.sendPrivateMessage(message);
        }
    }

    /**
     * Ensure that no team faces each other twice. This will not create an event as an error would occur in the code,
     * not due to user error.
     *
     * @param rounds List[Round] - The created rounds
     *
     * @return true if rounds are valid, false otherwise
     */
    private boolean validateRounds(List<Round> rounds) {
        System.out.println("Round size: " + rounds.size());
        if (rounds.size() <= 0) {
            return false;
        }

        Round roundOne = rounds.get(0);

        for (Match match : roundOne) {
            Team teamOne = match.getTeamOne();
            Team teamTwo = match.getTeamTwo();

            if (this.duplicateOpponent(teamOne, rounds)) {
                DuplicateOpponentErrorEvent event = new DuplicateOpponentErrorEvent(teamOne);
                this.eventManager.addEvent(event);
                return false;
            }

            if (this.duplicateOpponent(teamTwo, rounds)) {
                DuplicateOpponentErrorEvent event = new DuplicateOpponentErrorEvent(teamTwo);
                this.eventManager.addEvent(event);
                return false;
            }
        }

        return true;
    }

    /**
     *
     * Used to determine if the passed in team has any duplicate opponents. Every match in every round is iterated
     * through to make this determination.
     *
     * @param team Team - the team that needs determined if they have duplicate opponents
     * @param rounds List[Round] - the rounds that each team participates in.
     *
     * @return true if there is a duplicate opponent, false otherwise
     */
    private boolean duplicateOpponent(Team team, List<Round> rounds) {
        // KEY: teamName VALUE: times team is paired with opponent
        Map<String, Integer> opponentNum = new HashMap<>();

        for (Round round : rounds) {
            for (Match match : round) {
                // If the team in question is not part of the current match, continue
                if (!match.getTeamOne().equals(team) && !match.getTeamTwo().equals(team)) {
                    continue;
                }

                // Determine which team in the match is the team in question
                if (match.getTeamOne().equals(team)) {

                    // Increment the number of times the team has been paired with this opponent
                    String opponent = match.getTeamTwo().getTeamName();
                    if (!opponentNum.containsKey(opponent)) {
                        opponentNum.put(opponent, 1);
                    }
                    else {
                        int pairedWithNum = opponentNum.get(opponent);
                        pairedWithNum++;
                        opponentNum.put(opponent, pairedWithNum);
                    }
                }
                else if (match.getTeamTwo().equals(team)) {
                    String opponent = match.getTeamOne().getTeamName();

                    // Increment the number of times the team has been paired with this opponent
                    if (!opponentNum.containsKey(opponent)) {
                        opponentNum.put(opponent, 1);
                    }
                    else {
                        int pairedWithNum = opponentNum.get(opponent);
                        pairedWithNum++;
                        opponentNum.put(opponent, pairedWithNum);
                    }
                }
            }
        }

        // Determine if any opponent is paired with the team more than once
        Set<String> teamNames = opponentNum.keySet();
        for (String teamName : teamNames) {
            int pairedWithNum = opponentNum.get(teamName);

            if (pairedWithNum > 1) {
                Logger.log(teamName + " has been paired with " + team.getTeamName() + " " + pairedWithNum + " times", Level.WARNING);
                return true;
            }
        }
        return false;

    }

    private boolean isRoundOver() {
        for (Match match : this.rounds.get(this.currentRound - 1)) {
            if (!match.isDone()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void advanceTournament(MatchResult matchResult) {
        Round currentRound = this.rounds.get(this.currentRound - 1);
        MatchMetaData metaData = matchResult.getMetaData();

        for (Match match : currentRound) {
            if (metaData.getMatchId().equals(match.getMatchMetaData().getMatchId())) {
                match.setMatchResult(matchResult);
                break;
            }
        }

        for (Team team : this.teams) {
            if (team.containsMember(matchResult.getWinningTeam().get(0))) {
                team.addWin();
            }
            if (team.containsMember(matchResult.getLosingTeam().get(0))) {
                team.addLoss();
            }
        }

        this.bracketManager.updateBracket(this.rounds.get(this.currentRound - 1));

        // Determine if the Round is over
        if (this.isRoundOver()) {
            this.currentRound++;

            // Tournament is Over
            System.out.println("0 indexed current round: " + (this.currentRound - 1));
            System.out.println("Round size: " + this.rounds.size());
            if ((this.currentRound - 1) == this.rounds.size()) {
                this.tournamentWinner = this.teams.get(0);

                for (Team team : this.teams) {
                    if (this.tournamentWinner.getWins() < team.getWins()) {
                        this.tournamentWinner = team;
                    }
                }
                this.endTournament();
            }
            // Tournament is ongoing
            else {
                this.bracketManager.sendRoundToChannel(this.currentRound);
                this.sendCurrentRoundMatchCodes();
            }
        }


    }

    public List<Round> getRounds() {
        return rounds;
    }

    @Override
    public void setTournamentId(long tournamentId) {
        super.setTournamentId(tournamentId);
        for (Round round : this.rounds) {
            for (Match match : round) {
                match.setTournamentId(tournamentId);
            }
        }
    }

    @Override
    public void endTournament() {
        this.context.sendMessage(new TournamentWinnerEmbeddedMessageBuilder(this.tournamentWinner).buildMessageEmbed()).queue();
        this.isDone = true;
    }

    @Override
    public void idle() {
        this.isIdle = true;
    }

    @Override
    public void resume() {
        this.isIdle = false;

    }
}
