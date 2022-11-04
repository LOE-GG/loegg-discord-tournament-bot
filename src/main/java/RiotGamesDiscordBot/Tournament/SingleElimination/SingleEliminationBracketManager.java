package RiotGamesDiscordBot.Tournament.SingleElimination;

import RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration.BracketLeaf;
import RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration.BracketManager;
import RiotGamesDiscordBot.RiotGamesAPI.BracketGeneration.MatchImage;
import RiotGamesDiscordBot.Tournament.Match;
import RiotGamesDiscordBot.Tournament.Round;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class SingleEliminationBracketManager extends BracketManager {
    public SingleEliminationBracketManager() {
        super();
    }

    @Override
    public void generateBracket(List<Round> rounds) {
        Round firstRound = rounds.get(0);
        int totalRounds = (int)(Math.log(firstRound.getMatchSize() * 2) / Math.log(2));
        int matchNum = firstRound.getMatchSize();

        //Create the Buffered Image
        int imageHeight = (215 * (matchNum / 2)) + ((matchNum - 2) * 15);
        int imageWidth = (totalRounds * 240) + 200;
        this.bracketImage = new BufferedImage(imageWidth,
                imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        this.bracketGraphics = this.bracketImage.createGraphics();

        //Create and set up the graphics
        this.bracketGraphics.setColor(Color.GRAY);
        this.bracketGraphics.fillRect(0, 0, imageWidth, imageHeight);
        this.bracketGraphics.setColor(Color.WHITE);
        this.bracketGraphics.setFont(new Font("Times New Roman", Font.BOLD, 24));

        //Generate the Tournament Bracket
        for (int round = 0; round < totalRounds; round++) {
            createMatchImages(matchNum, round, imageHeight);
            matchNum = matchNum / 2;
        }

        //Add in the initial Teams
        for (int i = 0; i < this.matchImages.size(); i++) {
            MatchImage temp = this.matchImages.get(i);
            Match match = firstRound.getMatch(i);
            temp.setTeamOne(match.getTeamOne().getTeamName(), this.bracketGraphics);
            temp.setTeamTwo(match.getTeamTwo().getTeamName(), this.bracketGraphics);
        }

//        try {
//            sendBracketToChannel();
//        }
//        catch (IOException exception) {
//            exception.printStackTrace();
//        }

        int x = 0;
        int y = 0;

    }

    @Override
    public void updateBracket(Round round) {

    }

    private void createMatchImages(int matchNum, int round, int imageHeight) {
        int imageHeightFragment = imageHeight / matchNum;
        int centerMatchOffset = (imageHeightFragment / 2) - 50;

        if (matchNum == 1) {
            MatchImage matchImage1 = new MatchImage(this, (round * 240), centerMatchOffset);
            this.bracketGraphics.drawImage(matchImage1.getMatchImage(), null, matchImage1.getPositionX(), matchImage1.getPositionY());

            int xPosition = (round * 240) + 200;
            this.bracketGraphics.drawRect(xPosition, matchImage1.getPositionY() + 49, 25, 3);
            this.bracketGraphics.fillRect(xPosition, matchImage1.getPositionY() + 49, 25, 3);
            return;
        }

        for (int match = 0; match < matchNum; match += 2) {

            //Create the 2 matches to be connected by the leaf
            MatchImage matchImage1 = new MatchImage(this, (round * 240), (match * imageHeightFragment) + centerMatchOffset);
            this.bracketGraphics.drawImage(matchImage1.getMatchImage(), null, matchImage1.getPositionX(), matchImage1.getPositionY());

            MatchImage matchImage2 = new MatchImage(this, (round * 240), ((match + 1) * imageHeightFragment) + centerMatchOffset);
            this.bracketGraphics.drawImage(matchImage2.getMatchImage(), null, matchImage2.getPositionX(), matchImage2.getPositionY());

            //Create the leaf
            int topArmPosition = matchImage1.getPositionY() + 49;
            int bottomArmPosition = matchImage2.getPositionY() + 49;
            int xPosition = (round * 240) + 200;
            new BracketLeaf(xPosition, topArmPosition, bottomArmPosition, this.bracketGraphics);

            this.matchImages.add(matchImage1);
            this.matchImages.add(matchImage2);

        }
    }
}
