package eu.sshopencloud.marketplace.services;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class PaginatedResult {

    private long hits;

    private int count;

    private int page;

    private int perpage;

    private int pages;

}
