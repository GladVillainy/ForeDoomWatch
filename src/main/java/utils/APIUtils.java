package utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.nvd.NVDDTO;
import exceptions.ApiException;
import io.javalin.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String readAPI(String apiKey, String vendor, String product, String version) {

        String url = "https://services.nvd.nist.gov/rest/json/cves/2.0"
                + "?virtualMatchString=cpe:2.3:a:" + vendor + ":" + product + ":" + version;
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .header("apiKey", apiKey)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatus.OK.getCode()) {
                if(response.statusCode() == HttpStatus.TOO_MANY_REQUESTS.getCode()){
                    throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit have been exceeded");
                }
                throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public NVDDTO convertFromJson(String json) {
        try {
            return objectMapper.readValue(json, NVDDTO.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Could not read response from external API");
        }
    }
}
