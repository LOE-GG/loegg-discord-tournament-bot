package RiotGamesDiscordBot.Tournament;

import RiotGamesDiscordBot.RiotGamesAPI.Containers.MapType;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.PickType;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.SpectatorType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class TournamentConfig {
    private double teamSize;
    private boolean createChannels;
    private String metadata;
    private PickType pickType;
    private MapType mapType;
    private SpectatorType spectatorType;
    private TournamentType tournamentType;

    public TournamentConfig(File tournamentConfigFile) {
        //Enable default values.
        //If the try catch fails, those attributes set will remain what they were while the rest will have default values.
        this.tournamentType = TournamentType.SINGLE_ELIMINATION;
        this.teamSize = 5;
        this.pickType = PickType.BLIND_PICK;
        this.mapType = MapType.SUMMONERS_RIFT;
        this.spectatorType = SpectatorType.ALL;
        this.metadata = "Tournament";
        this.createChannels = true;

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(tournamentConfigFile));
            Sheet teamListSheet = workbook.getSheetAt(0);

            Row configRow = teamListSheet.getRow(11);

            this.tournamentType = TournamentType.valueOf(configRow.getCell(3).getStringCellValue().trim());
            this.teamSize = configRow.getCell(5).getNumericCellValue();
            this.pickType = PickType.valueOf(configRow.getCell(7).getStringCellValue().trim());
            this.mapType = MapType.valueOf(configRow.getCell(9).getStringCellValue().trim());
            this.spectatorType = SpectatorType.valueOf(configRow.getCell(11).getStringCellValue().trim());
            this.metadata = configRow.getCell(13).getStringCellValue().trim();

            String createChannels = teamListSheet.getRow(22).getCell(3).getStringCellValue().trim();
            this.createChannels = createChannels.equals("YES");
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    public double getTeamSize() {
        return teamSize;
    }

    public MapType getMapType() {
        return mapType;
    }

    public PickType getPickType() {
        return pickType;
    }

    public SpectatorType getSpectatorType() {
        return spectatorType;
    }

    public String getMetadata() {
        return metadata;
    }

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public boolean createChannels() {
        return this.createChannels;
    }
}
