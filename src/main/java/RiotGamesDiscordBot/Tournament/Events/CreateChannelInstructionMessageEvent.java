package RiotGamesDiscordBot.Tournament.Events;

import RiotGamesDiscordBot.EventHandling.Interactable;
import RiotGamesDiscordBot.EventHandling.MessageEvent;
import RiotGamesDiscordBot.EventHandling.TournamentResolvable;
import RiotGamesDiscordBot.Logging.DiscordLog.DiscordTextUtils;
import RiotGamesDiscordBot.Tournament.Tournament;

import java.util.Iterator;
import java.util.List;

public class CreateChannelInstructionMessageEvent extends MessageEvent implements Interactable {

    public CreateChannelInstructionMessageEvent(String tournamentName, Tournament tournament) {
        super("Tournament Channel Creation Instructions");

        this.message = "I have detected that you wish to create your own TextChannels for the " + tournamentName + "\n";
        this.message += "Each tournament I create requires 3 TextChannels. One for the current Tournament standings, One for Tournament Errors, and another so that I may send misc messages.\n";
        this.message += "I also require that they be placed under a new Category for organization sake. " +
                        "Please name these additions as follows....\n\n" +

                        DiscordTextUtils.underline("Category") + "\n\t" +
                            DiscordTextUtils.blockComment("[ Name of Tournament ] INFO") + "\n\n" +
                        DiscordTextUtils.underline("Text Channels") + "\n\t" +
                            DiscordTextUtils.blockComment("Error") + "\n\t" +
                            DiscordTextUtils.blockComment("Message") + "\n\t" +
                            DiscordTextUtils.blockComment("Current Standings") + "\n\n";

        this.message += "As a reminder, the Tournament Name is the contents of the MetaData section in the Tournament Config file. Once this has been completed, please respond back with\n\n\t" +
                    DiscordTextUtils.codeBlock("~lol -i [ Name of Tournament ] [ Event Id ] ") + "\n\n" + "to continue tournament creation.";
    }

    @Override
    public TournamentResolvable handleInteraction(Iterator<String> args) {

        this.resolved = true;
        this.pending = false;

        return (tournament -> {
            List<String> channelsNotFound = tournament.resetDiscordLoggerChannels();
            if (!channelsNotFound.isEmpty()) {
                this.resolved = false;
                this.pending = true;
                this.eventTitle = "Unable to find Tournament Text Channels";

                boolean categoryFound = !channelsNotFound.contains(tournament.getTournamentConfig().getMetadata());

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("I'm having some trouble finding ");

                if (!categoryFound) {
                    stringBuilder.append("the tournament info category");
                    if (channelsNotFound.size() > 1) {
                        stringBuilder.append("as well as the necessary text channels ");
                    }
                }
                else if(!channelsNotFound.isEmpty()) {
                    stringBuilder.append("the necessary text channels");
                }

                stringBuilder.append(" for tournament [ ").append(tournament.getTournamentConfig().getMetadata()).append(" ].\n\n");

                if (!categoryFound) {
                    stringBuilder.append("Please ensure that the category is created following the following format\n\n")
                            .append(DiscordTextUtils.blockComment("[ Name of Tournament ] INFO")).append("\n\n");
                }

                if (!channelsNotFound.isEmpty()) {
                    stringBuilder.append("Please ensure that the text channels are created with the following names inside the tournament category\n\n");
                    for (String channel : channelsNotFound) {
                        System.out.println("Channel: " + channel);
                        if (channel == null || channel.equals(tournament.getTournamentConfig().getMetadata())) {
                            continue;
                        }
                        stringBuilder.append(DiscordTextUtils.blockComment(channel)).append("\n\t");
                    }
                }

                stringBuilder.append("\nOnce the above has been completed, please respond back with the following command after you have completed the channel creation.\n\n")
                             .append(DiscordTextUtils.codeBlock("~lol -i [ Name of Tournament ] [ Event Id ] "));
                this.updateMessage(stringBuilder.toString());
            }
            else {
                // Resolution message
                this.resolved = true;
                this.eventTitle = "Successfully found channels";
                this.updateMessage("Thank you. I have found the necessary Category and Text Channels. Proceeding with tournament creation");
                tournament.eventUnIdle(this.eventId);
            }
        });
    }

    @Override
    public void setup(Tournament tournament) {
        tournament.eventIdle(this.eventId);
    }
}
