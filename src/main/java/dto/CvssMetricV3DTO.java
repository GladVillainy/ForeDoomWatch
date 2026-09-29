package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CvssMetricV3DTO(
        String source,
        String type,
        CvssDataDTO cvssData
) { }