package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity @AllArgsConstructor
@Setter @Getter @NoArgsConstructor
public class Roles {
    @Id
    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public Roles(String roleName) {
        this.roleName = roleName;
    }
}
