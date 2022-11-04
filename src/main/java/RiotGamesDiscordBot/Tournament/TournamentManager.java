package RiotGamesDiscordBot.Tournament;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchMetaData;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.Region;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.RiotGamesAPI.RiotGamesAPI;
import RiotGamesDiscordBot.Tournament.Events.InsufficientParticipantsEvent;
import RiotGamesDiscordBot.Tournament.RoundRobin.Events.Containers.MemberOnMultipleTeamsContainer;
import RiotGamesDiscordBot.Tournament.RoundRobin.Events.MemberOnMultipleTeamsEvent;
import RiotGamesDiscordBot.Tournament.RoundRobin.Events.TeamMemberDuplicateErrorEvent;
import com.google.gson.Gson;
import org.apache.commons.collections4.list.PredicatedList;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Semaphore;

public class TournamentManager extends Thread{

    private final Semaphore                     activeTournamentsSemaphore;
    private final List<Tournament>              activeTournaments;

    private final Semaphore                     registeredTournamentsSemaphore;
    private final List<Long>                    registeredTournaments;

    private final Semaphore                     idleTournamentsSemaphore;
    private final List<Tournament>              idleTournaments;

    private final Semaphore                     tournamentsAwaitingStartSemaphore;
    private final List<Tournament>              tournamentsAwaitingStart;

    private final Semaphore                     shutdownSemaphore;
    private       boolean                       shutdown;

    private final Semaphore                     interactionSemaphore;
    private final Map<String, Iterator<String>> interactions;

    private final Semaphore                     advanceTournamentSemaphore;
    private final List<MatchResult>             matchResults;

    private static TournamentManager Instance;

    private TournamentManager() {
        this.activeTournamentsSemaphore = new Semaphore(1);
        this.activeTournaments = new ArrayList<>();

        this.registeredTournamentsSemaphore = new Semaphore(1);
        this.registeredTournaments = new ArrayList<>();

        this.tournamentsAwaitingStartSemaphore = new Semaphore(1);
        this.tournamentsAwaitingStart = new ArrayList<>();

        this.idleTournamentsSemaphore = new Semaphore(1);
        this.idleTournaments = new ArrayList<>();

        this.shutdownSemaphore = new Semaphore(1);
        this.shutdown = false;

        this.interactionSemaphore = new Semaphore(1);
        this.interactions = new HashMap<>();

        this.advanceTournamentSemaphore = new Semaphore(1);
        this.matchResults = new ArrayList<>();
    }

    public static TournamentManager getInstance() {
        if (Instance == null) {
            Instance = new TournamentManager();
            Instance.start();
        }

        return Instance;
    }

    @Override
    public void run() {
        try {
            this.shutdownSemaphore.acquire();
            int iterations = 0;
            while (!this.shutdown) {
                System.out.println("Continuing Tournament loop: " + iterations);
                this.shutdownSemaphore.release();
                List<Tournament> deleteTournaments = new ArrayList<>();
                /* ***********************************************************************************************
                 * Check if any tournaments have resumed since last iteration
                 *************************************************************************************************/
                this.idleTournamentsSemaphore.acquire();
                List<Tournament> removeTournament = new ArrayList<>();
                for (Tournament tournament : this.idleTournaments) {
                    if (!tournament.isIdle()) {
                        // Check if the tournament is set up
                        removeTournament.add(tournament);
                        if (!tournament.isSetup()) {
                            this.awaitStart(tournament);
                        }
                        else {
                            Logger.log("95 - Activating tournament: " + tournament.getTournamentId(), Level.ERROR);
                            this.isActive(tournament);
                        }
                    }
                }

                for (Tournament tournament: removeTournament) {
                    this.idleTournaments.remove(tournament);
                }
                this.idleTournamentsSemaphore.release();


                /* ***********************************************************************************************
                 * Progress Any tournaments that need it
                 *************************************************************************************************/
                this.tournamentsAwaitingStartSemaphore.acquire();
                removeTournament.clear();
                for (Tournament tournament : this.tournamentsAwaitingStart) {
                    if (!tournament.isSetup()) {
                        System.out.println("Setting up tournament");
                        tournament.setup();
                    }

                    // Check if the tournament is registered. For a tournament to be registered it must be set up.
                    if (!this.activeTournaments.contains(tournament) && tournament.isSetup()) {
                        System.out.println("Registering tournament");
                        RegisterTournamentStatus status = this.registerTournament(tournament);
                        Logger.log("Registration status: " + status.getStatusCode(), Level.INFO);
                        if (status.getStatusCode() != RegisterTournamentStatusCode.OK) {
                            this.handleRegisterErrorStatus(status, tournament);

                            if (status.removeTournament()) {
                                deleteTournaments.add(tournament);
                            }

                        }
                    }

                    // Check if the tournament is started. In order for a tournament to start it must be set up and it must be registered
                    if (!tournament.isStarted() && this.registeredTournaments.contains(tournament.getTournamentId()) && tournament.isSetup()) {
                        System.out.println("Starting tournament");
                        tournament.start();
                        removeTournament.add(tournament);
                        this.isActive(tournament);
                    }
                }

                for (Tournament tournament : removeTournament) {
                    this.tournamentsAwaitingStart.remove(tournament);
                }

                this.tournamentsAwaitingStartSemaphore.release();

                /* **********************************************************************************************
                 * Check if any interactions need to be passed to a tournament
                 ************************************************************************************************/
                this.interactionSemaphore.acquire();

                Set<String> keySet = this.interactions.keySet();
                for (String key : keySet) {
                    Iterator<String> messageIterator = this.interactions.get(key);
                    for (Tournament tournament : this.idleTournaments) {
                        if (key.toLowerCase(Locale.ROOT).equals(tournament.getTournamentConfig().getMetadata().toLowerCase(Locale.ROOT))) {
                            tournament.passInteraction(messageIterator);
                            this.interactions.remove(key);
                            break;
                        }
                    }
                }

                this.interactionSemaphore.release();

                /* **********************************************************************************************
                 * Check if any tournaments need to be advanced
                 ************************************************************************************************/
                this.advanceTournamentSemaphore.acquire();
                List<MatchResult> removeMatchResultsFrom = new ArrayList<>();
                for (MatchResult matchResult : this.matchResults) {
                    MatchMetaData metaData = matchResult.getMetaData();
                    Logger.log(metaData.toString(), Level.INFO);
                    try {
                        // Find Tournament the metaData belongs to and advance it
                        this.activeTournamentsSemaphore.acquire();
                        Logger.log("Tournament list size: " + this.activeTournaments.size(), Level.INFO);
                        for (Tournament tournament : this.activeTournaments) {
                            Logger.log("Tournament in active : " + tournament.getTournamentConfig().getMetadata(), Level.INFO);
                            if (tournament.getTournamentId() == metaData.getTournamentId()) {
                                // Advance the tournament
                                tournament.advanceTournament(matchResult);

                                // If tournament advancement was successful, add to the list to be removed later.
                                removeMatchResultsFrom.add(matchResult);
                            }

                            if (tournament.isDone()) {
                                deleteTournaments.add(tournament);
                            }
                        }

                        this.activeTournamentsSemaphore.release();
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }

                // Remove the used match results from the list
                for (MatchResult matchResult : removeMatchResultsFrom) {
                    this.matchResults.remove(matchResult);
                }

                this.advanceTournamentSemaphore.release();

                /* **********************************************************************************************
                 * Check if any tournaments cannot continue
                 ************************************************************************************************/
                this.activeTournamentsSemaphore.acquire();
                for (Tournament tournament : this.activeTournaments) {
                    if (tournament.cannotContinue()) {
                        deleteTournaments.add(tournament);
                    }
                }
                this.activeTournamentsSemaphore.release();

                this.idleTournamentsSemaphore.acquire();
                for (Tournament tournament : this.idleTournaments) {
                    if (tournament.cannotContinue()) {
                        deleteTournaments.add(tournament);
                    }
                }
                this.idleTournamentsSemaphore.release();

                this.tournamentsAwaitingStartSemaphore.acquire();
                for (Tournament tournament : this.tournamentsAwaitingStart) {
                    if (tournament.cannotContinue()) {
                        deleteTournaments.add(tournament);
                    }
                }
                this.tournamentsAwaitingStartSemaphore.release();

                /* **********************************************************************************************
                 * Remove tournaments if needed
                 ************************************************************************************************/
                this.activeTournamentsSemaphore.acquire();
                this.idleTournamentsSemaphore.acquire();
                this.tournamentsAwaitingStartSemaphore.acquire();
                for (Tournament tournament : deleteTournaments) {
                    Logger.log("Removing tournament : " + tournament.getTournamentConfig().getMetadata(), Level.WARNING);
                    this.activeTournaments.remove(tournament);
                    this.idleTournaments.remove(tournament);
                    this.tournamentsAwaitingStart.remove(tournament);
                }
                this.activeTournamentsSemaphore.release();
                this.idleTournamentsSemaphore.release();
                this.tournamentsAwaitingStartSemaphore.release();

                iterations++;
                Thread.sleep(500);
                this.shutdownSemaphore.acquire();
            }
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            this.shutdownSemaphore.acquire();
            this.shutdown = true;
            this.shutdownSemaphore.release();


            //Shut down all tournament event manager threads
            this.activeTournamentsSemaphore.acquire();
            for (Tournament tournament : this.activeTournaments) {
                tournament.shutdownEventManager();
            }
            this.activeTournamentsSemaphore.release();

        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public RegisterTournamentStatus registerTournament(Tournament tournament) {
        RegisterTournamentStatus status = this.validateTournament(tournament);

        if (status.getStatusCode() == RegisterTournamentStatusCode.OK) {
            // Create a Tournament ID for the tournament after passing
            RiotGamesAPI riotGamesAPI = new RiotGamesAPI();
            try {
                int providerID = riotGamesAPI.getProviderID(new URL("https://loegg-tournent-bot.herokuapp.com/matchResult/"), Region.NA);
                long tournamentId = riotGamesAPI.getTournamentID(providerID, "New Tournament");
                tournament.setProviderId(providerID);
                tournament.setTournamentId(tournamentId);
                Logger.log("Tournament ID: " + tournament.getTournamentId(), Level.INFO);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // Register Tournament
                this.registeredTournamentsSemaphore.acquire();
                this.registeredTournaments.add(tournament.getTournamentId());
                this.registeredTournamentsSemaphore.release();
            }
            catch (InterruptedException exception) {
                exception.printStackTrace();
            }

        }

        return status;
    }

    private void handleRegisterErrorStatus(RegisterTournamentStatus status, Tournament tournament) {
        switch (status.getStatusCode()) {
            case DUPLICATE_MEMBERS_ON_TEAM:
                TeamMemberDuplicateErrorEvent teamMemberDuplicateErrorEvent = new TeamMemberDuplicateErrorEvent((SummonerInfo) status.getResource("duplicateMember"),
                        (Team) status.getResource("team"));
                tournament.eventManager.addEvent(teamMemberDuplicateErrorEvent);
                break;
            case DUPLICATE_PARTICIPANTS_IN_TOURNAMENT:
                MemberOnMultipleTeamsEvent memberOnMultipleTeamsEvent = new MemberOnMultipleTeamsEvent((MemberOnMultipleTeamsContainer) status.getResource("MemberOnMultipleTeamsContainer"));
                tournament.eventManager.addEvent(memberOnMultipleTeamsEvent);
                break;
            case NOT_ENOUGH_PARTICIPANTS:
                InsufficientParticipantsEvent insufficientParticipantsEvent = new InsufficientParticipantsEvent();
                tournament.eventManager.addEvent(insufficientParticipantsEvent);
                break;
            case NOT_SETUP:
            default:
                break;
        }
    }

    public Tournament getTournament(long tournamentId) {
        try {
            if (!this.registeredTournaments.contains(tournamentId)) {
                return null;
            }

            this.activeTournamentsSemaphore.acquire();
            for (Tournament tournament : this.activeTournaments) {
                if (tournament.getTournamentId() == tournamentId) {
                    this.activeTournamentsSemaphore.release();
                    return tournament;
                }
            }
            this.activeTournamentsSemaphore.release();


            this.idleTournamentsSemaphore.acquire();
            for (Tournament tournament : this.idleTournaments) {
                if (tournament.getTournamentId() == tournamentId) {
                    this.idleTournamentsSemaphore.release();
                    return tournament;
                }
            }
            this.idleTournamentsSemaphore.release();


            this.tournamentsAwaitingStartSemaphore.acquire();
            for (Tournament tournament : this.tournamentsAwaitingStart) {
                if (tournament.getTournamentId() == tournamentId) {
                    this.tournamentsAwaitingStartSemaphore.release();
                    return tournament;
                }
            }
            this.tournamentsAwaitingStartSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public void idleTournament(Tournament tournament) {
        tournament.setIdle(true);
        try {
            this.idleTournamentsSemaphore.acquire();
            this.idleTournaments.add(tournament);
            this.idleTournamentsSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void awaitStart(Tournament tournament) {
        try {
            this.tournamentsAwaitingStartSemaphore.acquire();
            this.tournamentsAwaitingStart.add(tournament);
            this.tournamentsAwaitingStartSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void isActive(Tournament tournament) {
        tournament.isStarted = true;
        try {
            this.activeTournamentsSemaphore.acquire();
            this.activeTournaments.add(tournament);
            this.activeTournamentsSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void advanceTournament(MatchResult matchResult) {
        try {
            this.advanceTournamentSemaphore.acquire();
            this.matchResults.add(matchResult);
            this.advanceTournamentSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Enforces all the rules required by RIOT games for a valid tournament as well as the following....
     *
     *      1. Ensures that all participants are unique.
     *      2. Ensures that all teams are unique.
     *
     * Tournament specific requirements should be validated elsewhere.
     *
     * @param tournament Tournament - the tournament to validate.
     * @return RegisterTournamentStatus - An object containing the status of the registration and helpful information about the error.
     */
    private RegisterTournamentStatus validateTournament(Tournament tournament) {

        // If the tournament is not setup just return
        if (!tournament.isSetup()) {
            return new RegisterTournamentStatus(RegisterTournamentStatusCode.NOT_SETUP);
        }

        // Check the total number of participants. Must be at least 20.
        int totalParticipants = 0;
        List<MemberOnMultipleTeamsContainer> members = new ArrayList<>();
        for (Team team: tournament.teams) {
            totalParticipants += team.size();

            // Ensure that all members on each team are unique.
            int duplicateMemberIndex = duplicateTeamMembers(team);
            if (duplicateMemberIndex >= 0) {
                RegisterTournamentStatus status = new RegisterTournamentStatus(RegisterTournamentStatusCode.DUPLICATE_MEMBERS_ON_TEAM);
                status.addResource("team", team);
                status.addResource("duplicateMember", team.getMembers().get(duplicateMemberIndex));
                status.setRemoveTournament(true);
                return status;
            }

            // Ensure that all members of the tournament are unique.
            for (SummonerInfo summonerInfo : team.getMembers()) {
                if (members.contains(summonerInfo)) {
                    MemberOnMultipleTeamsContainer container = members.get(members.indexOf(summonerInfo));
                    RegisterTournamentStatus status = new RegisterTournamentStatus(RegisterTournamentStatusCode.DUPLICATE_PARTICIPANTS_IN_TOURNAMENT);
                    status.addResource("MemberOnMultipleTeamsContainer", container);
                    status.setRemoveTournament(true);
                    return status;
                }
                else {
                    members.add(new MemberOnMultipleTeamsContainer(summonerInfo));
                }
            }
        }

        if (totalParticipants < 20) {
            RegisterTournamentStatus status = new RegisterTournamentStatus(RegisterTournamentStatusCode.NOT_ENOUGH_PARTICIPANTS);
            status.setRemoveTournament(true);

            return status;
        }

        return new RegisterTournamentStatus(RegisterTournamentStatusCode.OK);
    }


    /**
     *
     * Make sure that each member of the team is unique. No repeat members
     *
     * @param team Team - the team that is being validated
     *
     * @return Index of duplicate member, -1 otherwise.
     */
    private int duplicateTeamMembers(Team team) {
        List<SummonerInfo> members = team.getMembers();

        for (int i = 0; i < members.size(); i++) {
            SummonerInfo member = members.get(i);

            for (int j = 0; j < members.size(); j++) {
                // Don't let member be compared with itself
                if (j == i) {
                    continue;
                }
                SummonerInfo anotherMember = members.get(j);
                // If there is a repeat, the team is not valid
                if (member.equals(anotherMember)) {
                    return j;
                }
            }
        }

        return -1;
    }

    public void passInteraction(Iterator<String> messageIterator, String tournamentMetaData) {
        try {
            this.interactionSemaphore.acquire();
            this.interactions.put(tournamentMetaData, messageIterator);
            this.interactionSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
