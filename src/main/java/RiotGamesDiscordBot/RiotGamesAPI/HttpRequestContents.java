package RiotGamesDiscordBot.RiotGamesAPI;

import com.google.gson.Gson;

import java.net.URI;

public class HttpRequestContents {
    private final URI uri;
    private final RequestType requestType;
    private String requestBody;

    public HttpRequestContents(URI uri, RequestType requestType) {
        this.uri = uri;
        this.requestType = requestType;
        this.requestBody = "";
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public URI getUri() {
        return uri;
    }

    public void addRequestBody(Object requestBody) {
        this.requestBody = new Gson().toJson(requestBody);
    }

    public String getRequestBody() {
        return this.requestBody;
    }

}
