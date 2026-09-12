package entities;

import jakarta.persistence.*;

@Entity
public class Software {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "software_id")
    private long softwareId;

    @Column(name = "software_name")
    private String softwareName;

    @Column(name = "software_version")
    private String version;



    @ManyToOne
    @JoinColumn(name = "host_host_id")
    private Host host;
    
    public Host getHost() {return host;}

    public void setHost(Host host) {this.host = host;}
}
