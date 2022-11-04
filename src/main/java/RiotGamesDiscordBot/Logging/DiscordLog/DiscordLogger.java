package RiotGamesDiscordBot.Logging.DiscordLog;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;


public class DiscordLogger {

    private final String tournamentMetaData;
    private final Guild guild;
    private final TextChannel eventRequestChannel;
    private volatile Category tournamentCategory;
    private volatile TextChannel tournamentImageChannel;
    private volatile TextChannel errorChannel;
    private volatile TextChannel tournamentTextChannel;

    private final Semaphore errorChannelSemaphore;

    public DiscordLogger(TextChannel context, String tournamentMetaData) {
        this.tournamentMetaData = tournamentMetaData;
        this.guild = context.getGuild();
        this. errorChannelSemaphore = new Semaphore(1);
        this.eventRequestChannel = context;

        this.tournamentTextChannel = null;
        this.errorChannel = null;
        this.tournamentImageChannel = null;
        this.tournamentCategory = null;
    }

    public DiscordLogger(String tournamentMetaData, Guild guild, TextChannel context) {
        this.eventRequestChannel = context;
        this.tournamentMetaData = tournamentMetaData;
        this.guild = guild;
        this.errorChannelSemaphore = new Semaphore(1);
        this.errorChannel = null;

        // Find Tournament Category
        boolean tournamentCategoryAsync = true;
        List<Category> categories = guild.getCategories();
        for (Category category: categories) {
            if (category.getName().equals(this.tournamentMetaData + " INFO")) {
                this.tournamentCategory = category;
                tournamentCategoryAsync = false;
                break;
            }
        }

        // If tournament category not found, create it
        if (this.tournamentCategory == null) {
            tournamentCategoryAsync = true;
            this.guild.createCategory(this.tournamentMetaData + " INFO").queue((category -> {
                this.tournamentCategory = category;

                createTournamentErrorChannel(this.eventRequestChannel);
                createTournamentImageChannel(this.eventRequestChannel);
                createTournamentMessagesChannel(this.eventRequestChannel);
            }));
        }

        if (!tournamentCategoryAsync) {
            createTournamentErrorChannel(this.eventRequestChannel);
            createTournamentImageChannel(this.eventRequestChannel);
            createTournamentMessagesChannel(this.eventRequestChannel);
        }

    }


    private TextChannel findTextChannel(String name) {
        List<TextChannel> channels = this.tournamentCategory.getTextChannels();
        for (TextChannel channel : channels) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }

        return null;
    }

    private void createTournamentMessagesChannel(TextChannel context) {
        String channelName = "messages";
        this.tournamentTextChannel = findTextChannel(channelName);
        // Doesn't exist. Need to create it ourselves
        if (this.tournamentTextChannel == null) {
            this.tournamentCategory.createTextChannel(channelName).queue((textChannel -> {
                this.tournamentTextChannel = textChannel;
            }), (new ErrorHandler().handle(ErrorResponse.MISSING_PERMISSIONS, (event) -> {
                String title = DiscordTextUtils.colorRed("Unable to create messages channel") + "\n\n";
                String message = "Please give this bot permissions to create TextChannels or create your own Tournament Image channel with the the following name...." + "\n\n" +
                        DiscordTextUtils.blockComment("messages") + "\n\n" +
                        "It is expected that this TextChannel be created in a Category of the name....\n" +
                        "\t[ Name Of Tournament ] INFO";
                message += "\nOnce you have made this change, please reattempt to create the tournament";

                context.sendMessage(title + message).queue();
            })));
        }
    }

    private void createTournamentImageChannel(TextChannel context) {
        String channelName = "current-standings";
        this.tournamentImageChannel = findTextChannel(channelName);

        // Doesn't exist. Need to create it ourselves
        if (this.tournamentImageChannel == null) {
            this.tournamentCategory.createTextChannel(channelName).queue((textChannel -> {
                this.tournamentImageChannel = textChannel;
            }), (new ErrorHandler().handle(ErrorResponse.MISSING_PERMISSIONS, (event) -> {
                String title = DiscordTextUtils.colorRed("Unable to create current-standings channel") + "\n\n";
                String message = "Please give this bot permissions to create TextChannels or create your own Tournament Image channel with the the following name...." + "\n\n" +
                        DiscordTextUtils.blockComment("current-standings") + "\n\n" +
                        "It is expected that this TextChannel be created in a Category of the name....\n" +
                        "\t[ Name Of Tournament ] INFO";
                message += "\nOnce you have made this change, please reattempt to create the tournament";

                context.sendMessage(title + message).queue();
            })));
        }
    }

    private void createTournamentErrorChannel(TextChannel context) {

        String channelName = "error";
        this.errorChannel = this.findTextChannel(channelName);

        if (this.errorChannel == null) {
            this.tournamentCategory.createTextChannel(channelName).queue((textChannel -> {
                try {
                    errorChannelSemaphore.acquire();
                    errorChannel = textChannel;
                    errorChannelSemaphore.release();
                }
                catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }), new ErrorHandler().handle(ErrorResponse.MISSING_PERMISSIONS, (event) -> {
                String title = DiscordTextUtils.colorRed("Unable to create Error Channel") + "\n\n";
                String message = "Please give this bot permissions to create TextChannels or create your own Error channel with the following name..." + "\n\n" +
                        "error\n\n" +
                        "It is expected that this channel is created in a category of the name\n" +
                        DiscordTextUtils.blockComment("[ Name of Tournament ] INFO.");
                message += "\nWhere \"Name of Tournament\" is the contents of the MetaData section in the Tournament Config file. Once you have made this change, please reattempt to create the tournament";

                context.sendMessage(title + message).queue();
            }));
        }
    }

    public void sendMessage(String title, String message) {
        if (this.tournamentTextChannel == null) {
            this.eventRequestChannel.sendMessage(DiscordTextUtils.colorGreen(title) + "\n" + message).queue();
        }
        else {
            this.tournamentTextChannel.sendMessage(DiscordTextUtils.colorGreen(title) + "\n" + message).queue();
        }
    }

    public void sendErrorMessage(String title, String message) {
        Logger.log("Sending Error Message to channel: " + this.errorChannel.getName(), Level.INFO);
        if (this.errorChannel == null) {
            new Thread(() -> {
                long time = System.currentTimeMillis();
                while (errorChannel == null) {
                    if ((System.currentTimeMillis() - time) > 10000) {
                        return;
                    }

                    try {
                        this.errorChannelSemaphore.acquire();
                        this.errorChannel = this.findTextChannel(this.tournamentMetaData + "-tournament-error-channel");
                        this.errorChannelSemaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Error Channel was found later
                    errorChannel.sendMessage(DiscordTextUtils.colorRed(title) + "\n" + message).queue();
            }).start();
        }
        else {
            this.errorChannel.sendMessage(DiscordTextUtils.colorRed(title) + "\n" + message).queue();
        }
    }

    public TextChannel getTournamentImageChannel() {
        return tournamentImageChannel;
    }

    public List<String> resetChannels(String categoryName) {
        List<Category> categories = this.guild.getCategories();
        for (Category category : categories) {
            if (category.getName().toLowerCase(Locale.ROOT).contains(categoryName.toLowerCase(Locale.ROOT))) {
                this.tournamentCategory = category;
                break;
            }
        }

        List<String> channelsNotFound = new ArrayList<>();

        // Tournament category was not found. Could be due to a misspelling or it being missing.
        if (this.tournamentCategory == null) {
            channelsNotFound.add("error");
            channelsNotFound.add("messages");
            channelsNotFound.add("current-standings");
            channelsNotFound.add(this.tournamentMetaData);
            return channelsNotFound;
        }

        List<TextChannel> textChannels = this.tournamentCategory.getTextChannels();
        try {
            for (TextChannel textChannel : textChannels) {
                if (textChannel.getName().toLowerCase(Locale.ROOT).contains("error")) {
                    this.errorChannelSemaphore.acquire();
                    this.errorChannel = textChannel;
                    this.errorChannelSemaphore.release();
                }

                if (textChannel.getName().toLowerCase(Locale.ROOT).contains("message")) {
                    this.tournamentTextChannel = textChannel;
                }

                if (textChannel.getName().toLowerCase(Locale.ROOT).contains("standings")) {
                    this.tournamentImageChannel = textChannel;
                }
            }
        }
        catch (InterruptedException exception) {
            // Error occupied. was unsuccessful.
            channelsNotFound.add("error");
            channelsNotFound.add("messages");
            channelsNotFound.add("current-standings");
            return channelsNotFound;
        }

        if (this.errorChannel == null) {
            channelsNotFound.add("error");
        }

        if (this.tournamentTextChannel == null) {
            channelsNotFound.add("messages");
        }

        if (this.tournamentImageChannel == null) {
            channelsNotFound.add("current-standings");
        }

        return channelsNotFound;
    }
}

