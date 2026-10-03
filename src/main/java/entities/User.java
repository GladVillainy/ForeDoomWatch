package entities;

import jakarta.persistence.*;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;

@Entity @AllArgsConstructor
@NoArgsConstructor @Builder
@Getter @Table(name = "users") @Setter
public class User implements IEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    private String email;
    private String username;

    @Setter(AccessLevel.NONE)
    private String passwordHash;

    public User(String username, String email, String plainPassword) {
        this.username = username;
        this.email = email;
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public void changePassword(String newpswd){
        this.passwordHash = BCrypt.hashpw(newpswd, BCrypt.gensalt());

    }

    @Override
    public Long getID() {
        return userId;
    }
}
