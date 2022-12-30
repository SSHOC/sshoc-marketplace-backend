package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonValue;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import org.springframework.data.domain.Sort;

public enum ConceptSearchOrder {

    SCORE(false, "score"),

    LABEL(true, IndexConcept.LABEL_FIELD);

    private final boolean asc;
    private final String name;

    ConceptSearchOrder(boolean asc, String name) {
        this.asc = asc;
        this.name = name;
    }

    public boolean isAsc() {
        return this.asc;
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    public Sort toSort() {
        return Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, name);
    }
}
