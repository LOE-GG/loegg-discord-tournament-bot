package RiotGamesDiscordBot.RiotGamesAPI;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import RiotGamesDiscordBot.RiotAPIError;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters.ProviderRegistrationParameters;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters.TournamentCodeParameters;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.Parameters.TournamentRegistrationParameters;
import RiotGamesDiscordBot.RiotGamesAPI.Containers.Region;
import com.google.gson.Gson;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RiotGamesAPI {
    private static final String BASE_URL = "https://na1.api.riotgames.com/";
    private static final String REGIONAL_BASE_URL = "https://americas.api.riotgames.com/";


    public int getProviderID(URL callBackURL, Region region) throws IOException {
        try {
            URI uri;
            if (System.getenv("DEVELOPMENT").equals("TRUE")) {
                uri = new URI(REGIONAL_BASE_URL + "/lol/tournament-stub/v4/providers");
            }
            else {
                uri = new URI(REGIONAL_BASE_URL + "/lol/tournament/v4/providers");
            }
            HttpRequestContents requestContents = new HttpRequestContents(uri, RequestType.POST);
            requestContents.addRequestBody(new ProviderRegistrationParameters(region, callBackURL.toString()));
            HttpResponseContents responseContents = request(requestContents);
            return Integer.parseInt(responseContents.toString());
        }
        catch (URISyntaxException exception) {
            exception.printStackTrace();
        }

        return -1;

    }

    public int getTournamentID(int providerID, String tournamentName) throws IOException {
        try {
            URI uri;
            if (System.getenv("DEVELOPMENT").equals("TRUE")) {
                uri = new URI(REGIONAL_BASE_URL + "/lol/tournament-stub/v4/tournaments");
            }
            else {
                uri = new URI(REGIONAL_BASE_URL + "/lol/tournament/v4/tournaments");
            }
            HttpRequestContents requestContents = new HttpRequestContents(uri, RequestType.POST);
            requestContents.addRequestBody(new TournamentRegistrationParameters(tournamentName, providerID));
            HttpResponseContents responseContents = request(requestContents);
            return Integer.parseInt(responseContents.toString());
        }
        catch (URISyntaxException exception) {
            exception.printStackTrace();
        }

        return -1;
    }

    public String getTournamentCodes(long tournamentID, int tournamentCodeNum, TournamentCodeParameters tournamentCodeParameters) throws IOException {
        try {
            URI uri;
            if (System.getenv("DEVELOPMENT").equals("TRUE")) {
                uri = new URI(REGIONAL_BASE_URL + "/lol/tournament-stub/v4/codes?count=" + tournamentCodeNum + "&tournamentId=" + tournamentID);
            }
            else {
                uri = new URI(REGIONAL_BASE_URL + "/lol/tournament/v4/codes?count=" + tournamentCodeNum + "&tournamentId=" + tournamentID);
            }
            HttpRequestContents requestContents = new HttpRequestContents(uri, RequestType.POST);
            requestContents.addRequestBody(tournamentCodeParameters);
            HttpResponseContents responseContents = this.request(requestContents);
            return responseContents.toString();
        }
        catch (URISyntaxException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public String getSummonerInfoByName(String summonerName) throws IOException {
        try {
            URI uri = new URI(BASE_URL + "/lol/summoner/v4/summoners/by-name/" + summonerName.replace(" ", "%20"));
            HttpRequestContents requestContents = new HttpRequestContents(uri, RequestType.GET);
            HttpResponseContents responseContents = this.request(requestContents);
            return responseContents.toString();
        }
        catch (URISyntaxException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public String getSummonerRankInfoByEncryptedSummonerID(String encryptedSummonerId) throws IOException {
        try {
            URI uri = new URI(BASE_URL + "/lol/league/v4/entries/by-summoner/" + encryptedSummonerId);
            HttpRequestContents requestContents = new HttpRequestContents(uri, RequestType.GET);

            HttpResponseContents responseContents = request(requestContents);
            return responseContents.toString();
        }
        catch (URISyntaxException exception) {
            exception.printStackTrace();
        }

        return null;
    }



    private HttpResponseContents request(HttpRequestContents contents) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest request = getRequest(contents);
        request.addHeader("X-Riot-Token", System.getenv("RIOT_GAMES_API_KEY"));
        RiotAPIResponseHandler responseHandler = new RiotAPIResponseHandler();

        while (responseHandler.canAttempt()) {
            CloseableHttpResponse response = client.execute(request);
            responseHandler.handleResponse(response);
            responseHandler.handleEntity(response.getEntity());
        }

        if (!responseHandler.isSuccessful()) {
            RiotAPIError riotAPIError = new Gson().fromJson(new String(responseHandler.getResponseBytes(), StandardCharsets.UTF_8), RiotAPIError.class);
            if (riotAPIError.getStatusLine().statusCode == 404) {
                throw new SummonerNotFoundException(contents.getRequestBody());
            }
            Logger.log(contents.getUri() + "\n\t" + riotAPIError, Level.WARNING);
        }

        return new HttpResponseContents(responseHandler.getResponseBytes());
    }

    private HttpUriRequest getRequest(HttpRequestContents requestContents) {
        switch (requestContents.getRequestType()) {
            case GET:
                return new HttpGet(requestContents.getUri());
            case POST:
                HttpPost httpPost = new HttpPost(requestContents.getUri());
                try {
                    httpPost.setEntity(new StringEntity(requestContents.getRequestBody()));
                }
                catch(UnsupportedEncodingException exception) {
                    exception.printStackTrace();
                }
                return httpPost;
            case PATCH:
            default:
                return new HttpPatch(requestContents.getUri());
        }
    }
}
