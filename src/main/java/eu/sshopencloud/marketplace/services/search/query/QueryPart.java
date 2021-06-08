package eu.sshopencloud.marketplace.services.search.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class QueryPart {

    private String expression;

    private boolean quotedPhrase;

    public QueryPart() {
    }

    public QueryPart(String expression, boolean quotedPhrase) {
        this.quotedPhrase = quotedPhrase;
        if (quotedPhrase) {
            this.expression = "\"" + expression + "\"";
        } else {
            this.expression = expression;
        }
    }

}
