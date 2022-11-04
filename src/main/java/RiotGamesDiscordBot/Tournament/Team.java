package RiotGamesDiscordBot.Tournament;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.SummonerName;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Team implements Iterable<SummonerInfo>{
    private final List<SummonerInfo> members;
    private  final String teamName;

    private int participantNum;
    private int wins;
    private int losses;

    public Team(String teamName) {
        this.members = new ArrayList<>();
        this.teamName = teamName;

        this.wins = 0;
        this.losses = 0;
    }

    public String getWinLossRatio() {
        return wins + ":" + losses;
    }

    public void addWin() {
        this.wins++;
    }

    public void addLoss() {
        this.losses++;
    }

    public void addMember(SummonerInfo member) {
        this.members.add(member);
        this.participantNum++;
    }

    public int size() {
        return this.participantNum;
    }

    public int getLosses() {
        return losses;
    }

    public int getWins() {
        return wins;
    }

    public List<SummonerInfo> getMembers() {
        return members;
    }

    public String getTeamName() {
        return teamName;
    }

    public boolean containsMember(String summonerID) {
        for (SummonerInfo summonerInfo : this.members) {
            if (summonerInfo.getEncryptedSummonerId().equals(summonerID)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsMember(SummonerName summonerName) {
        for (SummonerInfo summonerInfo : this.members) {
            if (summonerInfo.getSummonerName().equals(summonerName.summonerName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(this.teamName).append("\n");
        for (SummonerInfo member : this.members) {
            builder.append("\t").append(member.getSummonerName()).append("\n");
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object team) {
        if (team instanceof Team) {
            Team compare = (Team) team;
            return compare.getTeamName().equals(this.teamName);
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<SummonerInfo> iterator() {
        return this.members.iterator();
    }

    @Override
    public void forEach(Consumer<? super SummonerInfo> action) {
        this.members.forEach(action);
    }

    @Override
    public Spliterator<SummonerInfo> spliterator() {
        return this.members.spliterator();
    }
}
