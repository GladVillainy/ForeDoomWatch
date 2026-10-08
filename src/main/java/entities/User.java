package entities;

import jakarta.persistence.*;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity @AllArgsConstructor
@NoArgsConstructor @Builder
@Getter @Table(name = "users") @Setter
public class User implements IEntity<Long>, ISecurityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    private String email;
    private String username;


    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING) @Builder.Default
    private Set<Roles> roles = new HashSet<>();

    @Setter(AccessLevel.NONE)
    private String passwordHash;

    public User(String username, String email, String plainPassword) {
        this.roles = new HashSet<>();
        this.username = username;
        this.email = email;
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public void changePassword(String newpswd){
        this.passwordHash = BCrypt.hashpw(newpswd, BCrypt.gensalt());

    }

    //IEntity Interface
    @Override
    public Long getID() {
        return userId;
    }


    // ISecurity interface
    @Override
    public Set<String> getRolesAsStrings() {
        return roles.stream()
                .map(Roles::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean verifyPassword(String pw) {
        return BCrypt.checkpw(pw, this.passwordHash);
    }

    @Override
    public void addRole(Roles role) {
        roles.add(role);
    }

    @Override
    public void removeRole(String role) {
        roles.remove(Roles.valueOf(role));
    }
}
