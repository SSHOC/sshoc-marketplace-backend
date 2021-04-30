package eu.sshopencloud.marketplace.services.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class QueryPart {

    private String expression;

    private boolean complexPhrase;

}
