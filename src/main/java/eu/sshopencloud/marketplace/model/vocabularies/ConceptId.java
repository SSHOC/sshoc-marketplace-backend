package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConceptId implements Serializable {

    private String code;

    private String vocabulary;

}
