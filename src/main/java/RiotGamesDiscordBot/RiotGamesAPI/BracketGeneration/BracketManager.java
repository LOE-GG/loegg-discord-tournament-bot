package RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.Tournament.Match;
import RiotGamesDiscordBot.Tournament.Round;
import RiotGamesDiscordBot.Tournament.RoundRobin.Exception.TournamentChannelNotFound;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class BracketManager {
    protected BufferedImage bracketImage;
    protected Graphics2D bracketGraphics;

    protected TextChannel tournamentChannel;
    protected final File imageFile;

    protected final List<MatchImage> matchImages;


    public BracketManager() {
        this.matchImages = new ArrayList<>();
        this.imageFile = new File("/resources/bracketImage.png");
    }

    public abstract void generateBracket(List<Round> rounds) throws TournamentChannelNotFound;

    public abstract void updateBracket(Round round);
}
