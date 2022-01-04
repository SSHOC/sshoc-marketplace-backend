package eu.sshopencloud.marketplace.services.search.query;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class QueryParser {

    private List<String> QUERY_UNACCEPTABLE_CHARACTERS = List.of("-", "–");

    public List<QueryPart> parsePhrase(String phrase) {
        List<QueryPart> result = new ArrayList<>();
        StringBuilder expression = new StringBuilder();
        boolean insideQuote = false;
        StringTokenizer tokenizer = new StringTokenizer(phrase, " \"", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals(" ")) {
                if (insideQuote) {
                    expression.append(token);
                } else {
                    if (isAcceptableExpression(expression.toString())) {
                        result.add(new QueryPart(ClientUtils.escapeQueryChars(expression.toString()), false));
                    }
                    expression = new StringBuilder();
                }
            } else if (token.equals("\"")) {
                if (expression.length() > 0 && expression.substring(expression.length() - 1, expression.length()).equals("\\")) {
                    // escaped quote
                    expression.replace(expression.length() - 1, expression.length(), "\"");
                } else {
                    if (insideQuote) {
                        insideQuote = false;
                        if (isAcceptableExpression(expression.toString())) {
                            result.add(new QueryPart(ClientUtils.escapeQueryChars(expression.toString()), true));
                        }
                        expression = new StringBuilder();
                    } else {
                        insideQuote = true;
                    }
                }
            } else {
                expression.append(token);
            }
        }
        if (isAcceptableExpression(expression.toString())) {
            if (insideQuote) {
                result.add(new QueryPart(ClientUtils.escapeQueryChars(expression.toString()), true));
            } else {
                result.add(new QueryPart(expression.toString(), false));
            }
        }
        return result;
    }

    private boolean isAcceptableExpression(String expression) {
        return !expression.isEmpty() && hasAllUnacceptableCharacters(expression);
    }

    private boolean hasAllUnacceptableCharacters(String expression) {
        for (String character : QUERY_UNACCEPTABLE_CHARACTERS) {
            expression = expression.replace(character, "");
        }
        return !expression.isEmpty();
    }

}
