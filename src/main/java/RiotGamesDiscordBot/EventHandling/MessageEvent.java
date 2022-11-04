package RiotGamesDiscordBot.EventHandling;

public abstract class MessageEvent extends Event {

    private boolean messageSent;
    protected String message;

    public MessageEvent(String messageTitle) {
        super(messageTitle);

        this.messageSent = false;
        this.message = "";
    }

    public boolean messageWasSent() {
        return this.messageSent;
    }

    public void messageSent() {
        this.messageSent = true;
    }

    public String getMessage() {
        return this.message + "\n\nEvent ID: " + this.eventId;
    }

    public void updateMessage(String message) {
        this.messageSent = false;
        this.message = message;
    }
}
