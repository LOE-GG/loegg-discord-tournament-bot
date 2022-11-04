package RiotGamesDiscordBot.Tournament;


import java.util.HashMap;
import java.util.Map;

public class RegisterTournamentStatus {
    private final RegisterTournamentStatusCode statusCode;
    private final Map<String, Object> resources;
    private boolean removeTournament;

    public RegisterTournamentStatus(RegisterTournamentStatusCode statusCode) {
        this.resources = new HashMap<>();
        this.statusCode = statusCode;
        this.removeTournament = false;
    }

    public Object getResource(String key) {
        return this.resources.get(key);
    }

    public void addResource(String key, Object resource) {
        this.resources.put(key, resource);
    }

    public RegisterTournamentStatusCode getStatusCode() {
        return statusCode;
    }

    public boolean hasResource(String key) {
        return !(this.resources.get(key) == null);
    }

    public void setRemoveTournament(boolean removeTournament) {
        this.removeTournament = removeTournament;
    }

    public boolean removeTournament() {
        return this.removeTournament;
    }
}
