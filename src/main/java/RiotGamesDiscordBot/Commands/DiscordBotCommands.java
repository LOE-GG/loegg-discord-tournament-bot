package RiotGamesDiscordBot.Commands;

import RiotGamesDiscordBot.Commands.CommandHandlers.CommandHandler;
import RiotGamesDiscordBot.Commands.CommandHandlers.InteractionCommandHandler;
import RiotGamesDiscordBot.Commands.CommandHandlers.SummonerInfoCommandHandler;
import RiotGamesDiscordBot.Commands.CommandHandlers.TournamentCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class DiscordBotCommands extends ListenerAdapter {
    private final JDA discordAPI;

    public DiscordBotCommands(JDA discordAPI) {
        this.discordAPI = discordAPI;
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        Iterator<String> messageIterator = Arrays.stream(message).iterator();
        String command = messageIterator.next();
        //League of Legends command
        if (command.equals("~lol")) {
            CommandHandler commandHandler;
            if (messageIterator.hasNext()) {
                String arg2 = messageIterator.next();
                switch (arg2) {
                    //Summoner Info
                    case "-si":
                        commandHandler = new SummonerInfoCommandHandler(event, messageIterator);
                        break;
                    //Tournament Start
                    case "-t":
                        System.out.println("Creating Tournament");
                        commandHandler = new TournamentCommandHandler(event, messageIterator);
                        break;
                    case "-i":
                        System.out.println("Handling interaction");
                        commandHandler = new InteractionCommandHandler(event, messageIterator);
                        break;
                    default:
                        commandHandler = new TournamentCommandHandler(event, messageIterator);
                }
                commandHandler.handle();
            } else {
                event.getChannel().sendMessage("Please provide a command.").queue();
            }
        }
    }

    @Override
    public void onCategoryCreate(@NotNull CategoryCreateEvent event) {
        System.out.println("CategoryCreate event: " + event);
    }

    @Override
    public void onTextChannelCreate(@NotNull TextChannelCreateEvent event) {
        System.out.println("TextChannelCreate event: " + event);
    }
}

