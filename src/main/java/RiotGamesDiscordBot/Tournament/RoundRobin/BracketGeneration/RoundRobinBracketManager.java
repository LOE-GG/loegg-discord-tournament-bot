package RiotGamesDiscordBot.Tournament.RoundRobin.BracketGeneration;

import RiotGamesDiscordBot.Logging.DiscordLog.DiscordLogger;
import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration.BracketManager;
import RiotGamesDiscordBot.Tournament.Match;
import RiotGamesDiscordBot.Tournament.Round;
import RiotGamesDiscordBot.Tournament.RoundRobin.Exception.TournamentChannelNotFound;
import RiotGamesDiscordBot.Tournament.Team;
import RiotGamesDiscordBot.TournamentBotApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.juli.logging.Log;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class RoundRobinBracketManager extends BracketManager  {
    private final List<RoundImage> roundImages;
    private final List<Team> teams;
    private String currentStandingMessageId;

    public RoundRobinBracketManager(List<Team> teams, TextChannel tournamentChannel) {
        super();
        this.roundImages = new ArrayList<>();
        this.teams = teams;
        this.tournamentChannel = tournamentChannel;
    }

    @Override
    public void generateBracket(List<Round> rounds) {


        /*##################################################################################################
         * Create Round Images
         *#################################################################################################*/

        // The amount of round images needed

        for (Round round : rounds) {
            Logger.log("Creating RoundImage : " + round.getRoundNum(), Level.INFO);
            try {
                this.roundImages.add(new RoundImage(round));
            } catch (IOException exception) {
                Logger.log("Error generating RoundImage " + round.getRoundNum(), Level.ERROR);
                exception.printStackTrace();
            }
        }

        Logger.log("Finished creating Round Images", Level.INFO);

        /*#############################################################################################
         * Create Current Standings Image
         *############################################################################################*/
        this.generateStandings();

        this.bracketGraphics.dispose();

        try {
            ImageIO.write(this.bracketImage, "png", new File("src/main/resources/currentStandings/current_standing.png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    public void generateStandings() {
        int numTeams = this.teams.size();
        int imageHeight = ((numTeams + 1) * 200) + ((numTeams + 1) * 40);
        int imageWidth = 1880;
        this.bracketImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        this.bracketGraphics = this.bracketImage.createGraphics();
        this.bracketGraphics.setColor(Color.GRAY);
        this.bracketGraphics.fillRect(0, 0, this.bracketImage.getWidth(), this.bracketImage.getHeight());
        try {
            BufferedImage title = ImageIO.read(new File("src/main/resources/Current-Standings-Title.png"));
            title = this.makeRoundedCorner(title);
            this.bracketGraphics.drawImage(title, null, 40, 20);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        int yPos = 240;
        for (int i = 0; i < this.teams.size(); i += 2) {
            Team teamOne = this.teams.get(i);
            try {
                BufferedImage teamOneStanding = ImageIO.read(new File("src/main/resources/Team-Standing.png"));
                Graphics2D teamStandingGraphics = teamOneStanding.createGraphics();

                // Write Team One Name
                teamStandingGraphics.setFont(new Font("SansSerif", Font.BOLD, 100));
                teamStandingGraphics.drawString(teamOne.getTeamName(), 50, 150);

                // Draw Starting Win Number
                teamStandingGraphics.drawString(String.valueOf(teamOne.getWins()), 1325, 150);

                // Draw Starting Loss Number
                teamStandingGraphics.drawString(String.valueOf(teamOne.getLosses()), 1675, 150);
                teamStandingGraphics.dispose();

                teamOneStanding = this.makeRoundedCorner(teamOneStanding);

                this.bracketGraphics.drawImage(teamOneStanding, null, 40, yPos);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }

            yPos += 240;

            // Write Team Two Name
            Team teamTwo = this.teams.get(i + 1);
            try {
                BufferedImage teamTwoStanding = ImageIO.read(new File("src/main/resources/Team-Standing.png"));
                Graphics2D teamStandingGraphics = teamTwoStanding.createGraphics();

                // Write Team Two Name
                teamStandingGraphics.setFont(new Font("SansSerif", Font.BOLD, 100));
                teamStandingGraphics.drawString(teamTwo.getTeamName(), 50, 150);

                // Draw Starting Win Number
                teamStandingGraphics.drawString(String.valueOf(teamTwo.getWins()), 1325, 150);

                // Draw Starting Loss Number
                teamStandingGraphics.drawString(String.valueOf(teamTwo.getLosses()), 1675, 150);
                teamStandingGraphics.dispose();

                teamTwoStanding = this.makeRoundedCorner(teamTwoStanding);

                this.bracketGraphics.drawImage(teamTwoStanding, null, 40, yPos);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }

            yPos += 240;
        }

    }

    private BufferedImage makeRoundedCorner(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    @Override
    public void updateBracket(Round currentRound) {
        // Update Round Image first
        RoundImage roundImage = this.roundImages.get(currentRound.getRoundNum() - 1);
        this.tournamentChannel.deleteMessageById(roundImage.getMessageID()).queue();
        this.sendRoundToChannel(currentRound.getRoundNum());

        this.generateStandings();
        try {
            ImageIO.write(this.bracketImage, "png", new File("src/main/resources/currentStandings/current_standing.png"));
            this.bracketGraphics.dispose();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        this.tournamentChannel.deleteMessageById(this.currentStandingMessageId).queue();
        this.sendCurrentStandings();
    }

    public void sendRoundToChannel(int roundNum) {
        RoundImage round = this.roundImages.get(roundNum - 1);
        this.tournamentChannel.sendMessage("Round " + round.getRoundNum()).addFile(round.generateImage()).complete();
        List<Message> messageID = this.tournamentChannel.getHistory().retrievePast(10).complete();
        round.setMessageID(messageID.get(0).getId());
    }

    public void sendCurrentStandings() {
        this.tournamentChannel.sendMessage("Current Standings").addFile(new File("src/main/resources/currentStandings/current_standing.png")).complete();
        List<Message> messageIDs = this.tournamentChannel.getHistory().retrievePast(10).complete();
        this.currentStandingMessageId = messageIDs.get(0).getId();
    }
}
