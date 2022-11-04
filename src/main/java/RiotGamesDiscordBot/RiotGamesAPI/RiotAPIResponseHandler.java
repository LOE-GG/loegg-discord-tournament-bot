package RiotGamesDiscordBot.RiotGamesAPI;

import RiotGamesDiscordBot.Logging.Level;
import RiotGamesDiscordBot.Logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RiotAPIResponseHandler {
    private boolean canAttempt;
    private boolean successful;
    private byte[] responseBytes;

    public RiotAPIResponseHandler() {
        this.canAttempt = true;
        this.successful = false;
    }

    public boolean canAttempt() {
        return this.canAttempt;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public byte[] getResponseBytes() {
        return responseBytes;
    }

    public void handleResponse(CloseableHttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case 400: //Bad Request
                System.out.println("400 Bad Request");
                canAttempt = false;
                successful = false;
                break;
            case 401: //Unauthorized
            case 404: //Not Found
                canAttempt = false;
                successful = false;
                break;
            case 403: //Forbidden
                System.out.println("Forbidden. Did you refresh the API token?");
                canAttempt = false;
                successful = false;
                break;
            case 415: //Unsupported Media Type
                System.out.println("Unsupported Media Type");
                canAttempt = false;
                successful = false;
                break;
            case 429: //Rate Limit Exceeded
                this.handle429(response);
                break;
            case 500: //Internal Server Error
            case 503: //Service Unavailable
            default:
                canAttempt = false;
                successful = true;
        }
    }

    public void handleEntity(HttpEntity entity) throws IOException {
        this.responseBytes = entity.getContent().readAllBytes();
        EntityUtils.consumeQuietly(entity);
    }

    private void handle429(CloseableHttpResponse response) {
        Header[] headers = response.getHeaders("Retry-After");
        int retryAfter = Integer.parseInt(headers[0].getElements()[0].getName());
        try {
            Thread.sleep(retryAfter * 1000L);
        }
        catch (InterruptedException exception) {
            Logger.log("Encountered InterruptedException in response to 429 Error Code", Level.WARNING);
        }

        this.canAttempt = true;
        this.successful = false;
    }

}
