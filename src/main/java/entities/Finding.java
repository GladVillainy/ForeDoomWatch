package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Entity @Getter @ToString
@NoArgsConstructor
public class Finding {
    @Id @Column(name = "finding_id")
    private Long findingID;


    private FindingStatus status;

    @Column(name = "detected_at")
    private Timestamp detectedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "resolved_at")
    private Timestamp resolvedAt;

    public Finding(FindingStatus status, Timestamp detectedAt,
                   Timestamp updatedAt, Timestamp resolvedAt) {
        this.status = status;
        this.detectedAt = detectedAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    public void setStatus(FindingStatus status) {
        this.status = status;
    }

    public void setDetectedAt(Timestamp detectedAt) {
        this.detectedAt = detectedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }


}
