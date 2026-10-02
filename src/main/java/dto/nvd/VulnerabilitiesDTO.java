package dto.nvd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VulnerabilitiesDTO(
        CveDTO cve
) { }