package eu.sshopencloud.marketplace.model.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "ease_of_use")
@Data
@NoArgsConstructor
public class EaseOfUse {

    @Id
    protected String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    protected Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
