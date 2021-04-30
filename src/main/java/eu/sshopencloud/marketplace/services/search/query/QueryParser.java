package eu.sshopencloud.marketplace.services.search.query;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class QueryParser {

    public List<QueryPart> parseQuery(String phrase, boolean advanced) {
        log.debug("Original query: {}", phrase);

        List<QueryPart> result = new ArrayList<QueryPart>();
        String[] words = phrase.split(" ", -1);

        boolean complexPhrase = false;
        String expression = "";

        for (String word : words) {
            if (!word.isEmpty()) {
                if (complexPhrase) {
                    if (word.endsWith("\"")) {
                        expression += " " + word;
                        result.add(createQueryPart(expression, true, advanced));
                        expression = "";
                        complexPhrase = false;
                    } else {
                        expression += " " + word;
                    }
                } else {
                    if (word.startsWith("\"")) {
                        expression += word;
                        complexPhrase = true;
                        if (word.endsWith("\"")) {
                            result.add(createQueryPart(expression, true, advanced));
                            expression = "";
                            complexPhrase = false;
                        }
                    } else {
                        expression = word;
                        result.add(createQueryPart(expression, false, advanced));
                        expression = "";
                    }
                }
            }
        }
        log.debug("Combined query: {}", result);
        return result;
    }

    private QueryPart createQueryPart(String expression, boolean complexPhrase, boolean advanced) {
        if (advanced) {
            return new QueryPart(expression, complexPhrase);
        }
        expression = escapeQueryExpression(expression, complexPhrase);
        return new QueryPart(expression, complexPhrase);
    }

    private String escapeQueryExpression(String expression, boolean complexPhrase) {
        if (!complexPhrase) {
            return ClientUtils.escapeQueryChars(expression);
        }

        // If phrase then remove the quotes (the first and last quotes should not be escaped) and restore at the end
        expression = expression.substring(1, expression.length() - 1);
        expression = expression.replaceAll("\"", "\\\"");
        expression = expression.replaceAll(" ", "\\ ");
        expression = "\"" + expression + "\"";

        return expression;
    }
}
