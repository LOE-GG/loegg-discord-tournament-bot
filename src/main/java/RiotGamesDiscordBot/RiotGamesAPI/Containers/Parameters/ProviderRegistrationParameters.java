package RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.Region;

public class ProviderRegistrationParameters {
    private final String url;
    private final String region;

    public ProviderRegistrationParameters(Region region, String callbackURL) {
        this.url = callbackURL;
        this.region = region.name();
    }
}
