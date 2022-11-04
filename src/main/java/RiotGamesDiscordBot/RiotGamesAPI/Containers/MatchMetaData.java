package RiotGamesDiscordBot.RiotGamesAPI.Containers;

public class MatchMetaData {
    private long tournamentId;
    private final String matchId;

    public MatchMetaData(long tournamentId, String matchId) {
        this.tournamentId = tournamentId;
        this.matchId = matchId;
    }

    public String getMatchId() {
        return matchId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof MatchMetaData) {
            return (this.tournamentId == ((MatchMetaData) object).getTournamentId()) && (this.matchId.equals(((MatchMetaData) object).getMatchId()));
        }

        return false;
    }

    @Override
    public String toString() {
        return "Tournament ID: " + this.tournamentId + "\nMatch ID: " + this.matchId;
    }
}
