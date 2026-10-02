package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NVDDTO(
        List<VulnerabilitiesDTO> vulnerabilities
) { }