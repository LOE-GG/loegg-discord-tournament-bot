package RiotGamesDiscordBot.RiotGamesAPI.EmbeddedMessages;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import RiotGamesDiscordBot.Tournament.Team;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.ArrayList;

public class TournamentWinnerEmbeddedMessageBuilder implements EmbeddedMessageBuilder {

    private final Team winner;

    public TournamentWinnerEmbeddedMessageBuilder(Team winner) {
        this.winner = winner;
    }

    @Override
    public MessageEmbed buildMessageEmbed() {

        MessageEmbed.Thumbnail thumbnail = new MessageEmbed.Thumbnail("http://ddragon.leagueoflegends.com/cdn/6.8.1/img/map/map11.png", null, 250, 250);
        MessageEmbed.Provider provider = new MessageEmbed.Provider("Riot Games", "https://www.riotgames.com/en");
        List<MessageEmbed.Field> fields = new ArrayList<>();
        MessageEmbed.Field winLoss = new MessageEmbed.Field("Win Loss Ratio", winner.getWinLossRatio(), true, true);
        fields.add(winLoss);

        StringBuilder stringBuilder = new StringBuilder();
        for (SummonerInfo summonerInfo : this.winner) {
            stringBuilder.append(summonerInfo.getSummonerName()).append('\n');
        }


        return new MessageEmbed("", "Congratulations!", stringBuilder.toString(), EmbedType.RICH,
                OffsetDateTime.now(), Color.CYAN.getRGB(), thumbnail, provider,
                new MessageEmbed.AuthorInfo(winner.getTeamName(), null, null, null), null,
                null, null, fields);
    }
}
