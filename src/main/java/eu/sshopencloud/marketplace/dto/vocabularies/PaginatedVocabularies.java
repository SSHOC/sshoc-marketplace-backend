package eu.sshopencloud.marketplace.dto.vocabularies;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedVocabularies extends PaginatedResult<VocabularyBasicDto> {

    private List<VocabularyBasicDto> vocabularies;

    @Override
    @JsonGetter("vocabularies")
    public List<VocabularyBasicDto> getResults() {
        return vocabularies;
    }
}