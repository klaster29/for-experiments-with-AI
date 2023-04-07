package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class KVTaskClient {
    private final String serverUrl;
    private final String apiToken;

    public KVTaskClient(String serverUrl) throws IOException {
        this.serverUrl = serverUrl;
        this.apiToken = register();
    }

    private String register() throws IOException {
        String url = serverUrl + "/register";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                return new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            }
        } else {
            throw new RuntimeException("Failed to register to server, response code: " + connection.getResponseCode());
        }
    }

    public void put(String key, String json) throws IOException {
        String url = serverUrl + "/save/" + key + "?API_TOKEN=" + apiToken;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to put data to server, response code: " + connection.getResponseCode());
        }
    }

    public String load(String key) throws IOException {
        String url = serverUrl + "/load/" + key + "?API_TOKEN=" + apiToken;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                return new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            }
        } else {
            throw new RuntimeException("Failed to load data from server, response code: " + connection.getResponseCode());
        }
    }

    public String getApiToken() {
        return apiToken;
    }
}

