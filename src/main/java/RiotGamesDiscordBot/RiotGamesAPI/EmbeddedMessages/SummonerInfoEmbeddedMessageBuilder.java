package RiotGamesDiscordBot.RiotGamesAPI.EmbeddedMessages;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.RankedInfo;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SummonerInfo;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SummonerInfoEmbeddedMessageBuilder implements EmbeddedMessageBuilder {
    protected static final String DATA_DRAGON_BASE_URL = "http://ddragon.leagueoflegends.com/cdn/10.20.1/";

    private static final String PROFILE_ICON_BASE_URL = DATA_DRAGON_BASE_URL + "img/profileicon/";
    private static final String OP_GG_BASE_URL = "https://na.op.gg/summoner/userName=";

    SummonerInfo info;
    String url;
    String title;
    String description;
    EmbedType type;
    OffsetDateTime timeStamp;
    int color;

    MessageEmbed.Thumbnail thumbnail;
    MessageEmbed.Provider siteProvider;
    MessageEmbed.AuthorInfo authorInfo;
    MessageEmbed.VideoInfo videoInfo;
    MessageEmbed.Footer footer;
    MessageEmbed.ImageInfo image;

    List<MessageEmbed.Field> fields;

    public SummonerInfoEmbeddedMessageBuilder(SummonerInfo info) {
        this.info = info;
        this.fields = formatFields();
        this.url = OP_GG_BASE_URL + info.getSummonerName().replace(" ", "+");

        this.title = this.info.getSummonerName();
        this.description = "Summoner Information about " + this.info.getSummonerName();
        this.type = EmbedType.RICH;
        this.timeStamp = OffsetDateTime.now();
        this.color = Color.CYAN.getRGB();

        String profileIconUrl = PROFILE_ICON_BASE_URL + this.info.getSummonerIconId() + ".png";
        this.thumbnail = new MessageEmbed.Thumbnail(profileIconUrl, null, 250, 250);
        this.siteProvider = new MessageEmbed.Provider("Riot Games", "https://www.riotgames.com/en");
        this.authorInfo = new MessageEmbed.AuthorInfo("Riot Games API BOT", null, null, null);
        this.videoInfo = null;
        this.footer = new MessageEmbed.Footer("This is the footer.", null, null);
        this.image = new MessageEmbed.ImageInfo(null, null, 0, 0);
    }

    private ArrayList<MessageEmbed.Field> formatFields() {
        ArrayList<MessageEmbed.Field> fields = new ArrayList<>();

        for (RankedInfo rankedInfo : this.info.getRankedInfo()) {

            String rank = rankedInfo.rank;
            String tier = rankedInfo.tier;
            String queueType = "";

            if (rankedInfo.queueType.contains("SOLO")) {
                queueType = "Ranked Solo/Duo";
            }
            else {
                queueType = "Ranked Flex 5x5";
            }
            MessageEmbed.Field tempField = new MessageEmbed.Field(queueType, tier + " " + rank, true, true);
            fields.add(tempField);
        }

        return fields;
    }


    @Override
    public MessageEmbed buildMessageEmbed() {
        return new MessageEmbed(this.url, this.title, this.description, this.type, this.timeStamp, this.color,
                this.thumbnail, this.siteProvider, this.authorInfo, this.videoInfo, this.footer, this.image, this.fields);
    }
}
