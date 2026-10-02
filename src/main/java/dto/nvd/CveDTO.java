package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CveDTO(
        @JsonProperty("id")
        String cveId,
        List<DescriptionDTO> descriptions,
        List<ReferenceDTO> references,
        MetricsDTO metrics
) { }