package eu.sshopencloud.marketplace.model.actors;

import com.google.gson.annotations.Expose;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.hypersistence.utils.hibernate.type.json.JsonStringType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "actor_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Converts({ @Convert(attributeName = "json", converter = JsonStringType.class),
        @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)})
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

    @Type(JsonType.class)
    @Column(name = "history", columnDefinition = "jsonb")
    @Nullable
    @Expose
    private String history;
}
