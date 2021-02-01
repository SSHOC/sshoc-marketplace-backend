package eu.sshopencloud.marketplace.repositories.search;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class QueryParser {

    public List<QueryPart> parseQuery(String q, boolean advanced) {
        log.debug("Original query: {}", q);

        List<QueryPart> result = new ArrayList<QueryPart>();
        String[] words = q.split(" ", -1);

        boolean phrase = false;
        String expression = "";

        for (String word : words) {
            if (!word.isEmpty()) {
                if (phrase) {
                    if (word.endsWith("\"")) {
                        expression += " " + word;
                        result.add(createQueryPart(expression, true, advanced));
                        expression = "";
                        phrase = false;
                    } else {
                        expression += " " + word;
                    }
                } else {
                    if (word.startsWith("\"")) {
                        expression += word;
                        phrase = true;
                        if (word.endsWith("\"")) {
                            result.add(createQueryPart(expression, true, advanced));
                            expression = "";
                            phrase = false;
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

    private QueryPart createQueryPart(String expression, boolean phrase, boolean advancedSearch) {
        if (advancedSearch)
            return new QueryPart(expression, phrase);

        expression = escapeQueryExpression(expression, phrase);

        return new QueryPart(expression, phrase);
    }

    private String escapeQueryExpression(String expression, boolean phrase) {
        if (!phrase)
            return ClientUtils.escapeQueryChars(expression);

        // If phrase then remove the quotes (the first and last quotes should not be escaped) and restore at the end
        expression = expression.substring(1, expression.length() - 1);
        expression = expression.replaceAll("\"", "\\\"");
        expression = expression.replaceAll(" ", "\\ ");
        expression = "\"" + expression + "\"";

        return expression;
    }
}
