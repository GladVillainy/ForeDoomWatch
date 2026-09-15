package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Host implements IEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "host_id")
    private long hostId;

    @Column(name = "host_name")
    private String hostName;
    @Column(name = "host_Description")
    private String hostDescription;

    //Constructor & setters
    public Host(String hostName, String hostDescription) {
        this.hostName = hostName;
        this.hostDescription = hostDescription;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setHostDescription(String hostDescription) {
        this.hostDescription = hostDescription;
    }

    //Relation
    @ManyToOne
    @JoinColumn(name = "user_user_id")
    private User user;
    public void setUser(User user) {this.user = user;}

    @Override
    public Long getID() {
        return hostId;
    }
}
