package utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIReader {

    public String readAPI(String url) {
        try {
            // Create an HttpClient instance
            HttpClient client = HttpClient.newHttpClient();

            // Create a request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check the status code and print the response
            if (response.statusCode() != 200) {
                throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        public <T> T getWithJacksonGeneric(String url, Class<T> tClass) {
            try {
                JsonNode node = objectMapper.readTree(new URI(url).toURL());
                return objectMapper.treeToValue(node, tClass);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
