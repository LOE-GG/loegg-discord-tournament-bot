package RiotGamesDiscordBot.EventHandling;

import RiotGamesDiscordBot.Logging.DiscordLog.DiscordLogger;
import RiotGamesDiscordBot.Tournament.Tournament;

import java.util.*;
import java.util.concurrent.Semaphore;

public class EventManager extends Thread {
    private final Map<String, Event> events;
    private final Semaphore eventSemaphore;
    private boolean shutdown;
    private final Semaphore shutdownSemaphore;

    private final Map<String, Iterator<String>> interactions;
    private final Semaphore interactionsSemaphore;

    private final DiscordLogger discordLogger;
    private final Tournament tournament;


    public EventManager(DiscordLogger discordLogger, Tournament tournament) {
        this.events = new HashMap<>();
        this.eventSemaphore = new Semaphore(1);

        this.shutdown = false;
        this.shutdownSemaphore = new Semaphore(1);

        this.interactions = new HashMap<>();
        this.interactionsSemaphore = new Semaphore(1);

        this.discordLogger = discordLogger;
        this.tournament = tournament;
    }


    @Override
    public void run() {
        try {
            shutdownSemaphore.acquire();

            while (!shutdown) {
                shutdownSemaphore.release();

                List<String> removeEventIds = new ArrayList<>();
                List<String> removeInteractionIds = new ArrayList<>();
                // Handle the existing events by type. Interactable events need different handling than
                // message events.
                this.eventSemaphore.acquire();
                Set<String> keySet = this.events.keySet();
                for (String key : keySet) {
                    Event event = this.events.get(key);

                    // No matter the state of the event, remove it after expiration.
                    if (event.expired()) {
                        removeEventIds.add(event.getEventId());
                    }

                    if (event instanceof Interactable) {
                        this.interactionsSemaphore.acquire();
                        if (this.interactions.get(event.getEventId()) != null) {
                            TournamentResolvable resolvable = ((Interactable) event).handleInteraction(this.interactions.get(event.getEventId()));
                            resolvable.resolve(this.tournament);
                            removeInteractionIds.add(event.getEventId());
                        }
                        this.interactionsSemaphore.release();
                    }

                    if (event instanceof MessageEvent) {
                        if (!((MessageEvent) event).messageWasSent()) {
                            this.discordLogger.sendMessage(event.getEventTitle(), ((MessageEvent) event).getMessage());
                            ((MessageEvent) event).messageSent();
                        }
                    }

                    if (event instanceof ErrorEvent) {
                        System.out.println("Event is an ErrorEvent");
                        this.discordLogger.sendErrorMessage(event.getEventTitle(), ((ErrorEvent) event).getMessage());
                        removeEventIds.add(event.getEventId());
                    }

                    if (event.isResolved()) {
                        removeEventIds.add(event.getEventId());
                    }

                    // Remove all events in the removeEvent list from the master list
                    for (String eventId : removeEventIds) {
                        this.events.remove(eventId);
                    }

                    // Remove all events in the removeInteraction list from the master list
                    this.interactionsSemaphore.acquire();
                    for (String eventId : removeInteractionIds) {
                        this.interactions.remove(eventId);
                    }
                    this.interactionsSemaphore.release();

                }
                this.eventSemaphore.release();

                shutdownSemaphore.acquire();

                // Sleep for 0.5 seconds before looking at the event list again
                Thread.sleep(500);
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
            this.eventSemaphore.release();
            this.interactionsSemaphore.release();
            this.shutdownSemaphore.release();
        }
    }

    public DiscordLogger getDiscordLogger() {
        return this.discordLogger;
    }

    public void addEvent(Event event) {
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        event.setup(this.tournament);
        try {
            this.eventSemaphore.acquire();
            this.events.put(eventId, event);
            this.eventSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
            this.eventSemaphore.release();
        }
    }

    public void addInteraction(Iterator<String> message) {
        String eventId = message.next();
        try {
            this.interactionsSemaphore.acquire();
            this.interactions.put(eventId, message);
            this.interactionsSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
            this.interactionsSemaphore.release();
        }
    }

    public void shutDown() {
        try {
            shutdownSemaphore.acquire();
            shutdown = true;
            shutdownSemaphore.release();
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
            this.shutdownSemaphore.release();
        }
    }
}
