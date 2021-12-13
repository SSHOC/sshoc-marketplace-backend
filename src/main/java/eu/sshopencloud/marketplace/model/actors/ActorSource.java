package eu.sshopencloud.marketplace.model.actors;

import com.google.gson.annotations.Expose;
import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import lombok.*;

import javax.persistence.*;


@Entity
@Table(name = "actor_external_sources")
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ActorSource implements OrderableEntity<String> {

    @Id
    @Expose
    private String code;

    @Column(nullable = false)
    @Expose
    private String label;

    @Expose
    private int ord;

    @Basic
    @Column(nullable = true)
    @Expose
    private String urlTemplate;

    public ActorSource(String code, String label) {
        this.code = code;
        this.label = label;
        this.ord = 0;
    }

    public ActorSource(String code, String label, String urlTemplate) {
        this.code = code;
        this.label = label;
        this.ord = 0;
        this.urlTemplate = urlTemplate;
    }


    @Override
    public String getId() {
        return code;
    }
}
