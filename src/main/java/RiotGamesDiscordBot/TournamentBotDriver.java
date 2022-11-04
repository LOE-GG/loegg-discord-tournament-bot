package RiotGamesDiscordBot;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.MatchResult.MatchResult;
import RiotGamesDiscordBot.Tournament.Tournament;
import RiotGamesDiscordBot.Tournament.TournamentManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@RestController
public class TournamentBotDriver {

    @PostMapping(value = "/matchResult")
    public @ResponseBody
    String matchResult(@RequestBody String matchResultJson) {
        System.out.println("MatchResultJson: " + matchResultJson);
        MatchResult matchResult = new Gson().fromJson(matchResultJson, MatchResult.class);

        String prettyPrintJson = new GsonBuilder().setPrettyPrinting().create().toJson(matchResult);
        System.out.println(prettyPrintJson);

        TournamentManager.getInstance().advanceTournament(matchResult);
        return "Done";
    }

    @GetMapping(value = "/getTournament")
    public ResponseEntity<Object> getTournament(@RequestHeader Map<String, String> headers) {

        System.out.println("headers: " + headers);

        if (!headers.get("host").contains("localhost")) {
            return ResponseEntity.status(403).body("Permission Denied");
        }
        if (!headers.containsKey("tournamentId".toLowerCase(Locale.ROOT))) {
            return ResponseEntity.badRequest().body("Missing tournamentId");
        }
        long tournamentId = Long.parseLong(headers.get("tournamentId".toLowerCase(Locale.ROOT)));
        Tournament tournament = TournamentManager.getInstance().getTournament(tournamentId);

        if (tournament == null) {
            return ResponseEntity.status(404).body("Tournament Id (" + tournamentId + ") Not Found");
        }


        return ResponseEntity.ok().body(new Gson().toJson(tournament));
    }

    @RequestMapping(value = "/riot.txt")
    public ResponseEntity<Object> downloadFile() throws IOException  {
        String filename = "./riot.txt";
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        Logger.log("Retrieved file", Level.INFO);

        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        Logger.log("Sending response", Level.INFO);
        return ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(
        MediaType.parseMediaType("application/txt")).body(resource);
    }
}
