package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaginatedVocabularies extends PaginatedResult {

    public PaginatedVocabularies(Page<Vocabulary> vocabularies, int page, int perpage) {
        this.setVocabularies(vocabularies.getContent());
        this.setHits(vocabularies.getTotalElements());
        this.setCount(this.getVocabularies().size());
        this.setPage(page);
        this.setPerpage(perpage);
        this.setPages(vocabularies.getTotalPages());
    }

    private List<Vocabulary> vocabularies;

}