package RiotGamesDiscordBot.RiotGamesAPI;

import java.nio.charset.StandardCharsets;

public class HttpResponseContents {
    private final byte[] responseBody;

    public HttpResponseContents(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {
        return new String(this.responseBody, StandardCharsets.UTF_8);
    }

    public byte[] getResponseBody() {
        return responseBody;
    }
}
