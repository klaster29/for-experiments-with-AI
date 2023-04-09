package client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final HttpClient client;
    private final String url;
    private final String apiToken;

    public KVTaskClient(String url) {
        client = HttpClient.newHttpClient();
        this.url = url;
        apiToken = this.register();
    }

    private String register() {
        String token = "";

        URI uri = URI.create(url + "/register");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                token = response.body();
            } else {
                System.out.println("Ошибка, сервер вернул код состояния" + response.statusCode());
            }
        } catch (InterruptedException | IOException | NullPointerException e) {
            System.out.println("Ошибка, регистрация не удалась");
        }

        return token;
    }

    public void put(String key, String json) {
        URI uri = URI.create(url + "/save/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Значение по ключу " + key + " обновлено");
            } else {
                System.out.println("Ошибка, сервер вернул код состояния" + response.statusCode());
            }
        } catch (InterruptedException | IOException | NullPointerException e) {
            System.out.println("Ошибка, значение не обновлено");
        }
    }

    public String load(String key) {
        String value = "";

        URI uri = URI.create(url + "/load/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                value = response.body();
                System.out.println("Значение по ключу " + key + " получено");
            } else {
                System.out.println("Ошибка, сервер вернул код состояния" + response.statusCode());
            }
        } catch (InterruptedException | IOException | NullPointerException e) {
            System.out.println("Ошибка, такого ключа нет");
//            e.printStackTrace();
        }
        return value;
    }

    public String getApiToken() {
        return apiToken;
    }
}

