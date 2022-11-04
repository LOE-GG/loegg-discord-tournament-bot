package RiotGamesDiscordBot.Tournament;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordUser {
    private final User user;
    private PrivateChannel privateChannel;

    public DiscordUser(User user) {
        this.user = user;
        this.privateChannel = null;
    }

    public void sendPrivateMessage(String message) {
        if (this.user.hasPrivateChannel()) {
            this.privateChannel.sendMessage(message).queue();
        }
        else {
            this.user.openPrivateChannel()
                    .queue((privateChannel) -> {
                        this.privateChannel = privateChannel;
                        this.privateChannel.sendMessage(message).queue();
                    }, Throwable::printStackTrace);
        }
    }
}
