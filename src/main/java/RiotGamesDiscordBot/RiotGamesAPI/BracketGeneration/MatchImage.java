package RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MatchImage {
    private String teamOne;
    private String teamTwo;

    private final BracketManager manager;

    private final int imageWidth;
    private final int imageHeight;


    private final BufferedImage matchImage;
    private final Graphics2D matchGraphics;

    private int positionX;
    private int positionY;

    private final Color defaultColor;

    public MatchImage(BracketManager manager, int positionX, int positionY) {
        this.manager = manager;
        this.positionX = positionX;
        this.positionY = positionY;
        this.imageHeight = 100;
        this.imageWidth = 200;
        this.defaultColor = Color.BLACK;

        this.matchImage = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        this.matchGraphics = this.matchImage.createGraphics();

        drawFilledRectangle(0, this.imageWidth);
        drawFilledRectangle(50, this.imageWidth);
    }



    private void drawFilledRectangle(int yPosition, int width) {
        this.matchGraphics.drawRect(0, yPosition, width, 50);
        this.matchGraphics.setColor(new Color(197, 218, 252));
        this.matchGraphics.fillRect(0, yPosition, width, 50);
        this.matchGraphics.setColor(defaultColor);
        this.matchGraphics.drawRect(0, yPosition, width, 50);
    }

    public void setTeamOne(String teamOne, Graphics2D parentGraphics) {
        this.teamOne = teamOne;
        parentGraphics.setColor(defaultColor);

        int teamOneDrawLocationX = ((this.imageWidth - parentGraphics.getFontMetrics().stringWidth(teamOne)) / 2)
                + this.positionX;
        int teamOneDrawLocationY = ((this.imageHeight - parentGraphics.getFontMetrics().getHeight()) / 2) + this.positionY;

        parentGraphics.drawString(this.teamOne, teamOneDrawLocationX, teamOneDrawLocationY);
    }

    public void setTeamTwo(String teamTwo, Graphics2D parentGraphics) {
        this.teamTwo = teamTwo;
        parentGraphics.setColor(defaultColor);

        int teamTwoDrawLocationX = ((this.imageWidth - parentGraphics.getFontMetrics().stringWidth(this.teamTwo)) / 2)
                + this.positionX;
        int teamTwoDrawLocationY = ((this.imageHeight - parentGraphics.getFontMetrics().getHeight()) / 2)
                + (this.positionY + 50);

        parentGraphics.drawString(this.teamTwo, teamTwoDrawLocationX, teamTwoDrawLocationY);
    }

    public void updateImage() {

//        try {
//            this.manager.updateBrackets(this);
//        }
//        catch(IOException exception) {
//            exception.printStackTrace();
//        }
    }

    public BufferedImage getMatchImage() {
        return this.matchImage;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }
}
