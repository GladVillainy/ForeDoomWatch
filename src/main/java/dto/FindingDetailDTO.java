package dto;

import entities.FindingStatus;

import java.time.LocalDateTime;

public record FindingDetailDTO(
        Long findingId,
        FindingStatus status,
        LocalDateTime detectedAt,
        String cveId,
        Double cvssScore,
        String softwareName,
        String version,
        String hostName
) { }