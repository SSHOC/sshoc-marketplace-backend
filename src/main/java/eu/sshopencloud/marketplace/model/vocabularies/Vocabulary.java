package eu.sshopencloud.marketplace.model.vocabularies;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "vocabularies")
@Data
@NoArgsConstructor
public class Vocabulary extends DigitalObject {

    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH }, orphanRemoval = true)
    @JoinColumn
    @OrderColumn(name = "ord")
    private List<Concept> concepts;

}
