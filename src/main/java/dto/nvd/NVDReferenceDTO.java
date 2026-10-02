package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDReferenceDTO(
        String url,
        String source,
        List<String> tags
) { }