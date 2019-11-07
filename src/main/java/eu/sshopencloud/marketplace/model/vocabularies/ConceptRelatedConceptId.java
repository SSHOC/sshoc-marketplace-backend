package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConceptRelatedConceptId implements Serializable {

    private ConceptId subject;

    private ConceptId object;

}
