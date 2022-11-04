package RiotGamesDiscordBot.Commands.CommandHandlers;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.RankedInfo;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.RiotGamesAPI.EmbeddedMessages.EmbeddedMessageBuilder;
import RiotGamesDiscordBot.RiotGamesAPI.EmbeddedMessages.SummonerInfoEmbeddedMessageBuilder;
import RiotGamesDiscordBot.RiotGamesAPI.RiotGamesAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SummonerInfoCommandHandler extends CommandHandler {
    private final String summonerName;
    private final RiotGamesAPI riotGamesAPI;

    public SummonerInfoCommandHandler(GuildMessageReceivedEvent event, Iterator<String> messageIterator) {
        super(event, messageIterator);
        if (messageIterator.hasNext()) {
            this.summonerName = messageIterator.next();
        }
        else {
            this.summonerName = null;
        }
        this.riotGamesAPI = new RiotGamesAPI();
    }


    @Override
    public void handle() {
        Gson gson = new Gson();
        try {
            //Get Summoner Info
            SummonerInfo summonerInfo = gson.fromJson(riotGamesAPI.getSummonerInfoByName(summonerName), SummonerInfo.class);
            //Get ranked info for previous Summoner
            ArrayList<RankedInfo> rankedInfo = gson.fromJson(riotGamesAPI.getSummonerRankInfoByEncryptedSummonerID(summonerInfo.getEncryptedSummonerId()),
                    new TypeToken<ArrayList<RankedInfo>>() {}.getType());
            summonerInfo.setRankedInfo(rankedInfo);

            //Create the Embedded Message
            EmbeddedMessageBuilder summonerInfoEmbeddedMessageBuilder = new SummonerInfoEmbeddedMessageBuilder(summonerInfo);

            event.getChannel().sendMessage(summonerInfoEmbeddedMessageBuilder.buildMessageEmbed()).queue();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
