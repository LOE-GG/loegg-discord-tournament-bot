package RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters;

public class TournamentRegistrationParameters {
    private final String name;
    private final int providerId;

    public TournamentRegistrationParameters(String name, int providerId) {
        this.name = name;
        this.providerId = providerId;
    }
}
