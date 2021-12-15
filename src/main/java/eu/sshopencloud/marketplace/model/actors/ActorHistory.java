package eu.sshopencloud.marketplace.model.actors;

import com.google.gson.annotations.Expose;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "actor_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TypeDefs({ @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class ActorHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_history_generator")
    @SequenceGenerator(name = "actor_history_generator", sequenceName = "actor_history_id_seq")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "actor_history_actor_id_fk"))
    private Actor actor;

    @Column(nullable = false)
    private ZonedDateTime dateCreated;

    @Type(type = "jsonb")
    @Column(name = "history", columnDefinition = "jsonb")
    @Nullable
    @Expose
    private String history;
}
