package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReferenceDTO(
        String url,
        String source,
        List<String> tags
) { }