package RiotGamesDiscordBot.EventHandling;

import RiotGamesDiscordBot.Tournament.Tournament;

import java.time.ZonedDateTime;

public abstract class Event {
    protected String eventTitle;
    protected String eventId;
    protected ZonedDateTime occurrence;

    // States
    protected boolean resolved;
    protected boolean pending;

    public Event(String eventTitle) {
        this.eventTitle = eventTitle;
        this.eventId = "";
        this.occurrence = ZonedDateTime.now();
        this.resolved = false;
        this.pending = true;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public ZonedDateTime getOccurrence() {
        return occurrence;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isResolved() {
        return this.resolved;
    }

    public boolean isPending() {
        return this.pending;
    }

    /**
     * Determines if the event has expired. By default, all children of Event expire
     * 2 hours after occurrence.
     *
     * @return boolean - True if expired
     */
    public boolean expired() {
        return this.occurrence.plusHours(2).isBefore(ZonedDateTime.now());
    }

    public abstract void setup(Tournament tournament);
}
