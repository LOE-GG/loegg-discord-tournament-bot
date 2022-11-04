package RiotGamesDiscordBot.Tournament;

import RiotGamesDiscordBot.EventHandling.EventManager;
import RiotGamesDiscordBot.Logging.DiscordLog.DiscordLogger;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.RiotGamesAPI.Event.SummonerNotFoundErrorEvent;
import RiotGamesDiscordBot.RiotGamesAPI.RiotGamesAPI;
import RiotGamesDiscordBot.RiotGamesAPI.SummonerNotFoundException;
import RiotGamesDiscordBot.Tournament.Events.CreateChannelInstructionMessageEvent;
import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public abstract class Tournament {
    private long tournamentId;
    private int  providerId;

    private transient final Semaphore eventIdlingSemaphore;
    private transient final Semaphore idleSemaphore;

    protected boolean isSetup;
    protected boolean isDone;
    protected boolean isIdle;
    protected boolean isStarted;
    protected boolean cannotContinue;
    protected final List<String> eventIdling;
    protected final List<Team> teams;
    protected transient final Map<String, List<String>> preSetupTeams;
    protected final List<Match> activeMatches;
    protected transient final TextChannel context;
    protected transient final TournamentConfig tournamentConfig;
    protected transient final EventManager eventManager;
    protected transient final DiscordUser creator;


    public Tournament(TournamentConfig tournamentConfig, GuildMessageReceivedEvent event, Map<String, List<String>> teams, DiscordUser creator) {
        this.tournamentConfig = tournamentConfig;
        this.teams = new ArrayList<>();
        this.preSetupTeams = teams;
        this.activeMatches = new ArrayList<>();
        this.eventIdling = new ArrayList<>();
        this.context = event.getChannel();
        this.creator = creator;

        this.eventIdlingSemaphore = new Semaphore(1);
        this.idleSemaphore = new Semaphore(1);

        this.isDone = false;
        this.isSetup = false;
        this.isIdle = false;
        this.isStarted = false;
        this.cannotContinue = false;

        // Determine if the user wanted the bot to create the channels or do it themselves
        DiscordLogger discordLogger;
        if (this.tournamentConfig.createChannels()) {
            discordLogger = new DiscordLogger(this.tournamentConfig.getMetadata(), event.getGuild(), this.context);
            this.eventManager = new EventManager(discordLogger, this);
        }
        else {
            discordLogger = new DiscordLogger(this.context, this.tournamentConfig.getMetadata());
            CreateChannelInstructionMessageEvent channelCreateEvent = new CreateChannelInstructionMessageEvent(tournamentConfig.getMetadata(), this);
            this.eventManager = new EventManager(discordLogger, this);
            this.eventManager.addEvent(channelCreateEvent);
        }

        this.eventManager.start();

        TournamentManager.getInstance().idleTournament(this);
    }

    public Tournament(TournamentConfig tournamentConfig, TextChannel channel, Map<String, List<String>> teams, DiscordUser creator) {
        this.tournamentConfig = tournamentConfig;
        this.context = channel;
        this.teams = new ArrayList<>();
        this.preSetupTeams = teams;
        this.activeMatches = new ArrayList<>();
        this.eventIdling = new ArrayList<>();
        this.creator = creator;

        this.eventIdlingSemaphore = new Semaphore(1);
        this.idleSemaphore = new Semaphore(1);

        this.isDone = false;
        this.isIdle = false;
        this.isStarted = false;
        this.isSetup = false;
        this.cannotContinue = false;

        DiscordLogger discordLogger = new DiscordLogger(this.tournamentConfig.getMetadata(), this.context.getGuild(), this.context);
        this.eventManager = new EventManager(discordLogger, this);
        this.eventManager.start();
    }

    public abstract void setup();

    public abstract void start();

    public abstract void advanceTournament(MatchResult matchResult);

    public abstract void endTournament();

    public abstract void idle();

    public abstract void resume();

    public int getProviderId() {
        return providerId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public TournamentConfig getTournamentConfig() {
        return tournamentConfig;
    }

    public boolean isSetup() {
        return this.isSetup;
    }

    public boolean isStarted() {
        return this.isStarted;
    }

    public boolean isIdle() {
        boolean idle = true;
        try {
            this.idleSemaphore.acquire();
            idle = this.isIdle;
            this.idleSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        return idle;
    }

    protected void setUpTeams() {
        // Get full summoner information from RIOT
        Gson gson = new Gson();
        RiotGamesAPI api = new RiotGamesAPI();
        Set<String> keySet = preSetupTeams.keySet();
        for (String teamName : keySet) {
            List<String> summonerNames = preSetupTeams.get(teamName);
            Team team = new Team(teamName);
            for (String summonerName: summonerNames) {
                try {
                    SummonerInfo summonerInfo = gson.fromJson(api.getSummonerInfoByName(summonerName), SummonerInfo.class);
                    team.addMember(summonerInfo);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                    if (exception instanceof SummonerNotFoundException) {
                        SummonerNotFoundErrorEvent event = new SummonerNotFoundErrorEvent(summonerName);
                        this.eventManager.addEvent(event);
                    }

                    return;
                }
            }
            this.teams.add(team);
        }
    }

    public void setDone(boolean done) {
        this.isDone = done;
    }

    public void setIdle(boolean idle) {
        try {
            this.idleSemaphore.acquire();
            this.isIdle = idle;
            this.idleSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void eventIdle(String eventId) {
        try {
            this.eventIdlingSemaphore.acquire();
            this.eventIdling.add(eventId);
            this.eventIdlingSemaphore.release();

            this.idleSemaphore.acquire();
            this.isIdle = true;
            this.idleSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void eventUnIdle(String eventId) {
        System.out.println("EventId: " + eventId);
        try {
            this.eventIdlingSemaphore.acquire();
            System.out.println("Size of eventIdling before removal: " + this.eventIdling.size());

            for (String ids : this.eventIdling) {
                System.out.println("Event ID in list: " + ids);
            }

            this.eventIdling.remove(eventId);
            this.eventIdlingSemaphore.release();

            this.idleSemaphore.acquire();
            System.out.println("Size of eventIdling: " + this.eventIdling.size());
            if (this.eventIdling.isEmpty()) {
                this.isIdle = false;
            }
            this.idleSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void setCannotContinue(boolean cannotContinue) {
        this.cannotContinue = cannotContinue;
    }

    public void setSetup(boolean setup) {
        this.isSetup = setup;
    }

    public boolean cannotContinue() {
        return this.cannotContinue;
    }

    public void passInteraction(Iterator<String> message) {
        this.eventManager.addInteraction(message);
    }

    public void shutdownEventManager() {
        this.eventManager.shutDown();
    }

    public List<String> resetDiscordLoggerChannels() {
        String tournamentMetaData = this.tournamentConfig.getMetadata();
        return this.eventManager.getDiscordLogger().resetChannels(tournamentMetaData);
    }

    public List<Match> getActiveMatches() {
        return activeMatches;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Tournament) {
            return ((Tournament) object).tournamentId == this.tournamentId;
        }

        return false;
    }
}
