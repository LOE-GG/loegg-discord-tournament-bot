package RiotGamesDiscordBot.Commands.CommandHandlers;

import RiotGamesDiscordBot.Tournament.TournamentManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Iterator;

public class InteractionCommandHandler extends CommandHandler{


    public InteractionCommandHandler(GuildMessageReceivedEvent event, Iterator<String> messageIterator) {
        super(event, messageIterator);
    }

    @Override
    public void handle() {
        //Retrieve tournament name from messageIterator
        String tournamentMetaData = this.message.next();
        System.out.println("Tournament meta data: " + tournamentMetaData);
        TournamentManager.getInstance().passInteraction(this.message, tournamentMetaData);
    }
}
