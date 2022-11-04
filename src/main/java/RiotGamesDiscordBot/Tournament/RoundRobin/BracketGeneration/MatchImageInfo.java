package RiotGamesDiscordBot.Tournament.RoundRobin.BracketGeneration;

public class MatchImageInfo {
    private final int teamOneXPosition;
    private final int teamOneYPostion;

    private final int teamTwoXPosition;
    private final int teamTwoYPosition;

    public MatchImageInfo(int teamOneXPosition, int teamOneYPostion, int teamTwoXPosition, int teamTwoYPosition) {
        this.teamOneXPosition = teamOneXPosition;
        this.teamOneYPostion = teamOneYPostion;

        this.teamTwoXPosition = teamTwoXPosition;
        this.teamTwoYPosition = teamTwoYPosition;
    }

    public int getTeamOneXPosition() {
        return teamOneXPosition;
    }

    public int getTeamOneYPostion() {
        return teamOneYPostion;
    }

    public int getTeamTwoXPosition() {
        return teamTwoXPosition;
    }

    public int getTeamTwoYPosition() {
        return teamTwoYPosition;
    }
}
