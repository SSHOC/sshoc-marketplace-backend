package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonValue;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import org.springframework.data.domain.Sort;

public enum ActorSearchOrder {

    SCORE(false, "score"),

    NAME(true, IndexActor.NAME_FIELD);

    private final boolean asc;
    private final String name;

    ActorSearchOrder(boolean asc, String name) {
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
