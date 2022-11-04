package RiotGamesDiscordBot.Commands.CommandHandlers;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.Tournament.*;
import RiotGamesDiscordBot.Tournament.RoundRobin.RoundRobinTournament;
import RiotGamesDiscordBot.Tournament.SingleElimination.SingleEliminationTournament;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class TournamentCommandHandler extends CommandHandler {

    public TournamentCommandHandler(GuildMessageReceivedEvent event, Iterator<String> messageIterator) {
        super(event, messageIterator);
    }

    @Override
    public void handle() {
        List<Message.Attachment> attachments = this.event.getMessage().getAttachments();

        // Ensure an attachment has been attached
        if (attachments.isEmpty()) {
            this.event.getChannel().sendMessage("Please attach an Excel Document containing the teams participating").queue();
            return;
        }

        Message.Attachment teamListAttachment = attachments.get(0);

        File teamListFile = teamListAttachment.downloadToFile().join();
        String extension = FilenameUtils.getExtension(teamListFile.toString());

        // Ensure the attachment is the proper extension
        if (!extension.equals("xls") && !extension.equals("xlsx")) {
            this.event.getChannel().sendMessage("Please resend command with an attached .xls or .xlsx file").queue();
            return;
        }

        // Get the Team Names and Members
        Map<String, List<String>> teams = this.getTeamsFromExcelFile(teamListFile);
        //  There was an error with the tournament config file.
        if (teams == null) {
            return;
        }


        //Get Tournament Type
        TournamentConfig tournamentConfig = new TournamentConfig(teamListFile);
        Tournament tournament;
        switch (tournamentConfig.getTournamentType()) {
            case ROUND_ROBIN:
                tournament = new RoundRobinTournament(tournamentConfig, this.event, teams, new DiscordUser(event.getAuthor()));
                break;
            case SINGLE_ELIMINATION:
            default:
                tournament = new SingleEliminationTournament(tournamentConfig, this.event, teams, new DiscordUser(event.getAuthor()));
        }

        Logger.log("tournament awaiting start", Level.INFO);

        TournamentManager.getInstance().awaitStart(tournament);
    }

    public Map<String, List<String>> getTeamsFromExcelFile(File teamListFile) {
        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(teamListFile));
            Sheet teamListSheet = workbook.getSheetAt(0);
            Map<String, List<String>> teams = new HashMap<>();

            //Get Team Names
            Row teamNamesRow = teamListSheet.getRow(0);
            for (int colIndex = 0; colIndex < teamNamesRow.getLastCellNum(); colIndex++) {
                teams.put(teamNamesRow.getCell(colIndex).getStringCellValue(), new ArrayList<>());
            }

            //Get Team Members
            Set<String> keySet = teams.keySet();
            for (int rowIndex = 1; rowIndex < 6; rowIndex++) {
                Row row = teamListSheet.getRow(rowIndex);
                Iterator<String> keyIterator = keySet.iterator();
                for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                    String teamMember = row.getCell(colIndex).getStringCellValue();
                    if (teamMember.isEmpty()) {
                        break;
                    }

                    if (!keyIterator.hasNext()) {
                        break;
                    }

                    String teamName = keyIterator.next();
                    teams.get(teamName).add(teamMember);
                }
            }
            return teams;
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        return null;
    }


}
