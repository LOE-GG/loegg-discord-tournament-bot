package RiotGamesDiscordBot.Tournament.RoundRobin.BracketGeneration;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.Tournament.Match;
import RiotGamesDiscordBot.Tournament.Round;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class RoundImage {
    private static class MatchImage {
        private final List<Match> matches;
        private final File roundImageFile;

        private BufferedImage image;

        protected MatchImage(List<Match> matches, File roundImageFile) throws IOException {
            this.matches = matches;
            this.roundImageFile = roundImageFile;
            this.image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("MatchImage.png")));
        }

        protected BufferedImage generateImage() {
            Graphics2D graphics = this.image.createGraphics();
            graphics.setFont(new Font("SansSerif", Font.BOLD, 48));
            graphics.setColor(Color.BLACK);

            // Draw the Matches
            int yPos = 175;
            for (Match match : this.matches) {
                graphics.drawString(match.getTeamOne().getTeamName(), 125, yPos);
                graphics.drawString(match.getTeamTwo().getTeamName(), 1150, yPos);

                if (match.isDone()) {
                    try {
                        graphics.setColor(null);
                        BufferedImage win = ImageIO.read(new File("src/main/resources/roundImages/Win.png"));
                        BufferedImage loss = ImageIO.read(new File("src/main/resources/roundImages/Loss.png"));
                        if (match.getWinner().equals(match.getTeamOne())) {
                            graphics.drawImage(win, null, 600, yPos - 55);
                            graphics.drawImage(loss, null, 1625, yPos - 55);
                        }
                        else {
                            graphics.drawImage(win, null, 1625, yPos - 55);
                            graphics.drawImage(loss, null, 600, yPos - 55);
                        }
                        graphics.setColor(Color.BLACK);
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
                yPos += 147;
            }

            try {
                this.makeRoundedCorner();
                ImageIO.write(this.image, "png", this.roundImageFile);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }

            graphics.dispose();
            return this.image;
        }

        private void makeRoundedCorner() {
            int w = this.image.getWidth();
            int h = this.image.getHeight();
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
            g2.drawImage(this.image, 0, 0, null);

            g2.dispose();

            this.image = output;
        }



    }

    private final Round round;
    private final BufferedImage image;
    private final BufferedImage roundImage;
    private final int roundImageNum;
    private final List<File> imageFiles;
    private final File masterImage;
    private final List<MatchImage> roundImages;
    private String messageID;

    public RoundImage(Round round) throws IOException {
        this.round = round;
        this.imageFiles = new ArrayList<>();
        this.roundImages = new ArrayList<>();
        this.roundImageNum = Math.max(round.getMatchSize() / 4, 1);
        this.roundImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("MatchImage.png")));


        int width = (this.roundImage.getWidth() * this.roundImageNum) + (40 * this.roundImageNum + 1);
        int height = (this.roundImage.getHeight() + 300);
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < round.getMatchSize(); i++) {
            this.imageFiles.add(new File("src/main/resources/roundImages/round" + round.getRoundNum() + "_" + i + ".png"));
        }

        this.masterImage = new File("src/main/resources/roundImages/round" + round.getRoundNum() + ".png");
    }

    public File generateImage() {
        Graphics2D graphics = this.image.createGraphics();
        graphics.setColor(Color.GRAY);
        graphics.fillRect(0, 0, this.image.getWidth(), this.image.getHeight());
        graphics.setColor(Color.BLACK);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 80));

        FontMetrics metrics = graphics.getFontMetrics();
        String roundTitle = "Round " + this.round.getRoundNum();
        int roundWidth = metrics.stringWidth(roundTitle);
        int titleXPos = this.image.getWidth() - (this.image.getWidth() / 2) - (roundWidth / 2);
        int titleYPos = metrics.getHeight() + 5;
        graphics.drawString(roundTitle, titleXPos, titleYPos);

        Iterator<Match> matchIter = this.round.iterator();
        for (int i = 0; i < this.roundImageNum; i++) {
            List<Match> matches = new ArrayList<>();
            int j = 1;
            while (matchIter.hasNext()) {
                matches.add(matchIter.next());

                if (j % 4 == 0) {
                    break;
                }

                j++;
            }

            try {
                this.roundImages.add(new MatchImage(matches, this.imageFiles.get(i)));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }


        int xPos = 20;
        for (MatchImage matchImage : this.roundImages) {
            BufferedImage image = matchImage.generateImage();
            graphics.drawImage(image, null, xPos, 150);
            xPos += image.getWidth() + 20;
        }
        try {
            this.writeFile();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        graphics.dispose();
        this.roundImages.clear();
        return this.masterImage;

    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    private void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public int getRoundNum() {
        return this.round.getRoundNum();
    }

    private void writeFile() throws IOException {
        ImageIO.write(this.image, "png", this.masterImage);
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessageID() {
        return this.messageID;
    }

}
