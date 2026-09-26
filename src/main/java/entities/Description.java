package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Description {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
            @Column(name = "description_Id")
    Integer descriptionId;

    String lang;
    String value;

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
