package SimulateTournament;

import RiotGamesDiscordBot.Tournament.Match;
import RiotGamesDiscordBot.Tournament.Round;
import RiotGamesDiscordBot.Tournament.RoundRobin.RoundRobinTournament;
import SimulateTournament.Containers.MatchComplete;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class TestMain {

    public static void main(String[] args) throws IOException {
        URL url = new URL("http://localhost:8080/getTournament/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("origin", "localhost");
        connection.setRequestProperty("tournamentId", "2928");

        InputStream responseStream = connection.getInputStream();

        byte[] responseBytes = responseStream.readAllBytes();
        String responseJson = new String(responseBytes);
        connection.disconnect();
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        System.out.print("Tournament Type: ");

        String tournamentType = scanner.nextLine();  // Read user input
        System.out.println("tournament type: " + tournamentType);
        if (tournamentType.toLowerCase(Locale.ROOT).equals("Round Robin".toLowerCase(Locale.ROOT))) {
            TestRoundRobin(responseJson);
        }

    }

    public static void TestRoundRobin(String tournamentJson) throws IOException {
        RoundRobinTournament tournament = new Gson().fromJson(tournamentJson, RoundRobinTournament.class);

        String prettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(tournament);
        System.out.println(prettyPrint);

        List<Round> rounds = tournament.getRounds();
        System.out.println(tournamentJson);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Complete Match? ");
        String response = scanner.nextLine();

        for (Round round : rounds) {
            for (Match match : round) {

                if (response.toLowerCase(Locale.ROOT).equals("QUIT".toLowerCase(Locale.ROOT))) {
                    break;
                }

                MatchComplete matchComplete = MatchComplete.fromMatch(match);

                sendRequest(matchComplete);

                scanner = new Scanner(System.in);
                System.out.println("Complete Match? ");
                response = scanner.nextLine();
            }

            if (response.toLowerCase(Locale.ROOT).equals("QUIT".toLowerCase(Locale.ROOT))) {
                break;
            }

        }

        System.out.println("Simulation complete");

    }

    public static void sendRequest(MatchComplete matchComplete) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/matchResult/");

        StringEntity requestEntity = new StringEntity(new Gson().toJson(matchComplete));
        httpPost.setEntity(requestEntity);
        //Execute and get the response.
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();
    }
}
