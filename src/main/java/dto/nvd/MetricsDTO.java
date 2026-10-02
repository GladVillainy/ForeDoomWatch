package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetricsDTO(
        List<CvssMetricV3DTO> cvssMetricV30,
        List<CvssMetricV3DTO> cvssMetricV31
) { }