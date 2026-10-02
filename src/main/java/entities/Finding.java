package entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity @Getter @ToString @AllArgsConstructor
@NoArgsConstructor @Setter @Builder
public class Finding implements IEntity<Long> {
    @Id @Column(name = "finding_id") @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long findingId;

    @Enumerated(EnumType.STRING) @Builder.Default
    private FindingStatus status = FindingStatus.OPEN;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "software_id")
    private Software software;

    @ManyToOne
    @JoinColumn(name = "vulnerability_id")
    private Vulnerability vulnerability;

    @Override
    public Long getID() {
        return findingId;
    }
}
