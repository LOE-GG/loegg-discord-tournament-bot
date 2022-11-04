package RiotGamesDiscordBot.Commands.CommandHandlers;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Iterator;

public abstract class CommandHandler {
    Iterator<String> message;
    GuildMessageReceivedEvent event;

    public CommandHandler(GuildMessageReceivedEvent event, Iterator<String> messageIterator) {
        this.message = messageIterator;
        this.event = event;
    }

    public abstract void handle();
}
