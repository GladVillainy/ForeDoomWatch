package entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity @Setter @NoArgsConstructor
@AllArgsConstructor @Builder @Getter
public class Software implements IEntity<Long> {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "software_id")
    private long softwareId;

    @Column(name = "software_name")
    private String softwareName;

    @Column(name = "software_version")
    private String version;

    private String vendor;

    @OneToMany(mappedBy = "software", cascade = CascadeType.ALL)
    private List<Finding> finding;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private Host host;

    @Override
    public Long getID() {
        return softwareId;
    }
}
