package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDMetricsDTO(
        List<NVDCvssMetricV3DTO> cvssMetricV30,
        List<NVDCvssMetricV3DTO> cvssMetricV31
) { }