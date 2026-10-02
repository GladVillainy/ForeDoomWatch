package dto;

import entities.FindingStatus;

import java.time.LocalDateTime;

public record FindingDTO(
        Long findingId,
        FindingStatus status,
        LocalDateTime detectedAt,
        LocalDateTime updatedAt,
        LocalDateTime resovledAt

) {
}
