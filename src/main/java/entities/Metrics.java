package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Builder @NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class Metrics {

    @Column(name = "cvss_score")
    private Double cvssScore;

    @Column(name = "cvss_vector")
    private String cvssVector;

    @Column(name = "severity")
    private String severity;
}