package RiotGamesDiscordBot.Tournament;

import RiotGamesDiscordBot.Logging.ConsoleColors;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

public class Round implements Iterable<Match>{
    private final List<Match> matches;
    private final int roundNum;

    public Round(List<Match> matches, int roundNum) {
        this.matches = matches;
        this.roundNum = roundNum;
    }

    public int getRoundNum() {
        return roundNum;
    }

    public boolean isInProgress() {
        for (Match match : this.matches) {
            if (!match.isDone()) {
                return true;
            }
        }

        return false;
    }

    public int getMatchSize() {
        return this.matches.size();
    }

    public Match getMatch(int i) {
        return this.matches.get(i);
    }

    public void setMatch(int i, Match match) {
        this.matches.set(i, match);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Determine format width of column 1
        int columnOneWidth = 0;
        for (Match match : this.matches) {
            if (match.getTeamOne().getTeamName().length() > columnOneWidth) {
                columnOneWidth = match.getTeamOne().getTeamName().length();
            }
        }

        //Determine format width of column 2
        int columnTwoWidth = 0;
        for (Match match : this.matches) {
            if (match.getTeamTwo().getTeamName().length() > columnTwoWidth) {
                columnTwoWidth = match.getTeamTwo().getTeamName().length();
            }
        }

        String title ="Round " + this.roundNum;
        stringBuilder.append(ConsoleColors.GREEN_UNDERLINED).append(StringUtils.center(title, columnOneWidth + columnTwoWidth + 5)).append(ConsoleColors.RESET).append("\n");

        // Add team names
        for (Match match : this.matches) {
            stringBuilder.append(StringUtils.rightPad(match.getTeamOne().getTeamName(), columnOneWidth)).append("     ")
                         .append(StringUtils.rightPad(match.getTeamTwo().getTeamName(), columnTwoWidth)).append('\n');
        }

        return stringBuilder.append("\n\n").toString();
    }

    @Override
    public Iterator<Match> iterator() {
        return this.matches.iterator();
    }

    @Override
    public Spliterator<Match> spliterator() {
        return this.matches.spliterator();
    }
}
