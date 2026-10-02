package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDCvssDataDTO(
        Double baseScore,
        String vectorString,
        String baseSeverity
) {}