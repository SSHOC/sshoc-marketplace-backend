package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.model.licenses.License;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "vocabularies")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vocabulary {

    @Id
    protected String code;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = false, length = 4096)
    private String description;

    @Basic
    @Column(nullable = true)
    private String accessibleAt;

    @Transient
    private List<Concept> concepts;

}
