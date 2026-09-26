package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDDTO(
        List<Vulnerabilities> vulnerabilities
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vulnerabilities(
            Cve cve
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Cve(
                @JsonProperty("id")
                String cveId,
                List<Description> descriptions,
                List<Reference> references,
                Metrics metrics
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Description(
                    String lang,
                    String value
            ) {
            }
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Metrics(
                    List<CvssMetricV2> cvssMetricV2
            ) {
                @JsonIgnoreProperties(ignoreUnknown = true)
                public record CvssMetricV2(
                        String baseSeverity,
                        CvssData cvssData
                ) {
                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public record CvssData(
                            Double baseScore,
                            String vectorString
                    ) {
                    }
                }
            }
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Reference(
                    String url,
                    String source,
                    List<String> tags
            ) {
            }
        }
    }
}