package eu.sshopencloud.marketplace.repositories.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class QueryPart {

    private String expression;

    private boolean phrase;

}
