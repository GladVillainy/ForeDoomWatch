package entities;

import jakarta.persistence.*;
import lombok.*;
import security.entities.User;

import java.util.ArrayList;
import java.util.List;

@Entity @Setter @NoArgsConstructor
@AllArgsConstructor @Builder @Getter
public class Host implements IEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "host_id")
    private Long hostId;

    @Column(name = "host_name")
    private String hostName;
    @Column(name = "host_Description")
    private String hostDescription;

    //Constructor & setters
    public Host(String hostName, String hostDescription) {
        this.hostName = hostName;
        this.hostDescription = hostDescription;
    }



    //Relation
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public void setUser(User user) {this.user = user;}

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    private List<Software> softwareList;

    @Override
    public Long getID() {
        return hostId;
    }
}
