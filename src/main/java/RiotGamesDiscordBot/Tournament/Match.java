package RiotGamesDiscordBot.Tournament;

import RiotGamesDiscordBot.Logging.ConsoleColors;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchMetaData;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.SummonerName;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.Tournament.RoundRobin.BracketGeneration.MatchImageInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Match implements Iterable<Team> {
    private final Team teamOne;
    private final Team teamTwo;
    private MatchMetaData metaData;
    private String tournamentCode;

    private MatchImageInfo matchImageInfo;

    private Team winner;
    private Team loser;

    private boolean isDone;

    public Match(Team teamOne, Team teamTwo) {
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;

        this.winner = null;
        this.loser = null;
        this.isDone = false;
        this.metaData = null;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setMatchResult(MatchResult matchResult) {
        List<SummonerName> winningTeam = matchResult.getWinningTeam();

        if (this.teamOne.containsMember(winningTeam.get(0))) {
            this.winner = this.teamOne;
            this.loser = this.teamTwo;
        }
        else {
            this.loser = this.teamOne;
            this.winner = this.teamTwo;
        }

        this.isDone = true;
    }

    public void setTournamentCode(String tournamentCode) {
        this.tournamentCode = tournamentCode;
    }

    public void setMatchImageInfo(MatchImageInfo matchImageInfo) {
        this.matchImageInfo = matchImageInfo;
    }

    public void setMetaData(MatchMetaData metaData) {
        this.metaData = metaData;
    }

    public void setTournamentId(long tournamentId) {
        this.metaData.setTournamentId(tournamentId);
    }

    public MatchImageInfo getMatchImageInfo() {
        return matchImageInfo;
    }

    public Team getTeamOne() {
        return teamOne;
    }

    public Team getTeamTwo() {
        return teamTwo;
    }

    public String getTournamentCode() {
        return tournamentCode;
    }

    public Team getLoser() {
        return loser;
    }

    public Team getWinner() {
        return winner;
    }

    public MatchMetaData getMatchMetaData() {
        return metaData;
    }


    public boolean isMatch(MatchResult matchResult) {
        return this.teamOne.containsMember(matchResult.getWinningTeam().get(0)) || this.teamTwo.containsMember(matchResult.getWinningTeam().get(0));
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Match) {
            return this.metaData == ((Match) object).getMatchMetaData();
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        //Determine format String width for Team One
        int teamOneWidth = this.getFormatStringWidth(this.teamOne);
        String teamOneFormat = "%-" + teamOneWidth + "s";

        //Determine format String width for Team Two
        int teamTwoWidth = this.getFormatStringWidth(this.teamTwo);
        String teamTwoFormat = "%-" + teamTwoWidth + "s";

        List<SummonerInfo> teamOneMembers = this.teamOne.getMembers();
        List<SummonerInfo> teamTwoMembers = this.teamTwo.getMembers();

        // Title
        stringBuilder.append(ConsoleColors.GREEN_BOLD).append("Match : ").append(this.tournamentCode).append(ConsoleColors.RESET);

        // Team Names
        String teamOneName = StringUtils.center(this.teamOne.getTeamName(), teamOneWidth);
        String teamTwoName = StringUtils.center(this.teamTwo.getTeamName(), teamTwoWidth);
        stringBuilder.append('\n').append(teamOneName).append('\t').append('\t').append(teamTwoName).append('\n');

        // Team Members
        for (int i = 0; i < teamOneMembers.size() && i < teamTwoMembers.size(); i++) {
            String teamOneMember = String.format(teamOneFormat + "\t\t", teamOneMembers.get(i).getSummonerName());
            String teamTwoMember = String.format(teamTwoFormat + "\n", teamTwoMembers.get(i).getSummonerName());
            stringBuilder.append(teamOneMember).append(teamTwoMember);
        }

        return stringBuilder.toString();
    }

    /**
     * Determines the greatest length among member names on a Team. The width begins with the Team Name to
     * accommodate the case where the Team Name is the longest String
     *
     * @param team Team - the team needing the longest member summoner name determined
     * @return int - the largest width among team member summoner names
     */
    private int getFormatStringWidth(Team team) {
        List<SummonerInfo> members = team.getMembers();
        int width = team.getTeamName().length();
        for (SummonerInfo summonerInfo : members) {
            if (summonerInfo.getSummonerName().length() > width) {
                width = summonerInfo.getSummonerName().length();
            }
        }

        return width;
    }

    @NotNull
    @Override
    public Iterator<Team> iterator() {
        List<Team> teams = new ArrayList<>();
        teams.add(this.teamOne);
        teams.add(this.teamTwo);
        return teams.iterator();
    }

    @Override
    public void forEach(Consumer<? super Team> action) {
        List<Team> teams = new ArrayList<>();
        teams.add(this.teamOne);
        teams.add(this.teamTwo);
        teams.forEach(action);
    }

    @Override
    public Spliterator<Team> spliterator() {
        List<Team> teams = new ArrayList<>();
        teams.add(this.teamOne);
        teams.add(this.teamTwo);
        return teams.spliterator();
    }
}
