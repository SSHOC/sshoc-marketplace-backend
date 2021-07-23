package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ConceptCore extends ConceptBasicCore {

    private List<RelatedConceptCore> relatedConcepts;

}
