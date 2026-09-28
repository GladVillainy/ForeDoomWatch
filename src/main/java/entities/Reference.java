package entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Builder @NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Entity
public class Reference {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @Column(name = "refernce_Id")
    private Long refernceId;

    private String url;
    private String source;
    private List<String> tags;

    @ManyToOne
    @JoinColumn(name = "vulnerability_id")
    private Vulnerability vulnerability;


}
