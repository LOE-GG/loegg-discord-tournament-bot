package RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BracketLeaf {

    private final Graphics2D leafGraphics;

    private static final int LINE_WIDTH = 3;

    public BracketLeaf(int xPosition, int topArmPosition, int bottomArmPosition, Graphics2D graphics2D) {
        this.leafGraphics = graphics2D;
        this.leafGraphics.setColor(Color.BLACK);

        //Top Arm
        drawFilledRectangle(xPosition, topArmPosition, 25, LINE_WIDTH);

        //Bottom Arm
        drawFilledRectangle(xPosition, bottomArmPosition, 25, LINE_WIDTH);

        //Connects both arms
        drawFilledRectangle(xPosition + 25, topArmPosition, LINE_WIDTH, bottomArmPosition - topArmPosition + LINE_WIDTH);

        //Connecting Line to next round
        int centerOfArmConnector = ((bottomArmPosition - topArmPosition) / 2) + topArmPosition;
        drawFilledRectangle(xPosition + 25, centerOfArmConnector, 15, LINE_WIDTH);

    }

    private void drawFilledRectangle(int xPosition, int yPosition, int width, int height) {
        this.leafGraphics.drawRect(xPosition, yPosition, width, height);
        this.leafGraphics.fillRect(xPosition, yPosition, width, height);
    }
}
