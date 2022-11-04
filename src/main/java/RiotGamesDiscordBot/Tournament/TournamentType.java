package RiotGamesDiscordBot.Tournament;

public enum TournamentType {
    NONE, ROUND_ROBIN, SINGLE_ELIMINATION;

    public static TournamentType fromString(String name) {
        switch (name) {
            case "ROUND_ROBIN":
                return ROUND_ROBIN;
            case "SINGLE_ELIMINATION":
                return SINGLE_ELIMINATION;
            default:
                return NONE;
        }
    }
}
