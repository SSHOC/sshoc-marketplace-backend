package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ConceptCore extends ConceptBasicCore {

    private List<RelatedConceptCore> relatedConcepts;

}
