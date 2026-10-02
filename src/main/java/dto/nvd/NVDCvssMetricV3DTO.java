package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDCvssMetricV3DTO(
        String source,
        String type,
        NVDCvssDataDTO cvssData
) { }