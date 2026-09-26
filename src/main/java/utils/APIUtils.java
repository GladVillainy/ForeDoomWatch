package utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIUtils<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String readAPI(String apiKey, String search) {
        String url = "https://services.nvd.nist.gov/rest/json/cves/2.0"
                + "?keywordSearch=" + search
                + "&resultsPerPage=2";
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .header("apiKey", apiKey)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public T convertFromJson(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
