package eu.sshopencloud.marketplace.repositories.search;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class QueryParser {

    public List<QueryPart> parseQuery(String q) {
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
                        result.add(new QueryPart(expression, true));
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
                            result.add(new QueryPart(expression, true));
                            expression = "";
                            phrase = false;
                        }
                    } else {
                        expression = word;
                        result.add(new QueryPart(expression, false));
                        expression = "";
                    }
                }
            }
        }
        log.debug("Combined query: {}", result);
        return result;
    }

}
