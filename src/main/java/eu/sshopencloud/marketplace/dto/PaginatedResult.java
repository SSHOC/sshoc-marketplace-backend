package eu.sshopencloud.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class PaginatedResult<T> {

    private long hits;

    private int count;

    private int page;

    private int perpage;

    private int pages;

    public abstract List<T> getResults();
}
