package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDCveDTO(
        @JsonProperty("id")
        String cveId,
        List<NVDDescriptionDTO> descriptions,
        List<NVDReferenceDTO> references,
        NVDMetricsDTO metrics
) { }