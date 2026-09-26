package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NvdResponseDTO(
        List<Vulnerability> vulnerabilities
) {
    public record Vulnerability(
            Cve cve
    ) {
        public record Cve(
                @JsonProperty("id")
                String cveId,
                List<Description> descriptions,
                List<Reference> references,
                Metrics metrics
        ) {
            public record Description(
                    String lang,
                    String value
            ) {
            }

            public record Metrics(
                    List<CvssMetricV2> cvssMetricV2
            ) {
                public record CvssMetricV2(
                        String baseSeverity,
                        CvssData cvssData
                ) {
                    public record CvssData(
                            Double baseScore,
                            String vectorString
                    ) {
                    }
                }
            }

            public record Reference(
                    String url,
                    String source,
                    List<String> tags
            ) {
            }
        }
    }
}