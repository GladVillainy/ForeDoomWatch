package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CvssDataDTO(
        Double baseScore,
        String vectorString,
        String baseSeverity
) {}