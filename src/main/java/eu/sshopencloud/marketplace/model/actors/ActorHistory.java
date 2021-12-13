package eu.sshopencloud.marketplace.model.actors;

import com.google.gson.annotations.Expose;
import io.swagger.v3.core.util.Json;
import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "actor_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorHistory implements Serializable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="actor_history_actor_id_fk"))
    @Expose
    private Actor actor;

    @Id
    @Column(nullable = false)
    @Expose
    private ZonedDateTime dateCreated;

    @Column(name = "history", columnDefinition = "JSON")
    @Nullable
    @Expose
    private String history;
}
