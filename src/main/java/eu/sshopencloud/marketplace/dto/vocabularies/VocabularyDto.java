package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class VocabularyDto extends VocabularyBasicDto {

    @Nullable
    private String description;
    private PaginatedConcepts conceptResults;
}
