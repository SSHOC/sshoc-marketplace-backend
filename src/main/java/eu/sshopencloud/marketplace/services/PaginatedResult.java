package eu.sshopencloud.marketplace.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class PaginatedResult {

    private long hits;

    private int count;

    private int page;

    private int perpage;

    private int pages;

}
