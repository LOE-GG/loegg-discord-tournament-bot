package RiotGamesDiscordBot.EventHandling;

public abstract class ErrorEvent extends Event {

    protected String message;

    public ErrorEvent(String messageTitle) {
        super(messageTitle);
    }

    public String getMessage() {
        return message;
    }
}
