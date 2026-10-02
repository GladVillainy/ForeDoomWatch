package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDDescriptionDTO(
        String lang,
        String value
) { }