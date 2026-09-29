package entities;

import jakarta.persistence.*;
import lombok.*;

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
    private String password;

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    @Override
    public Long getID() {
        return userId;
    }
}
