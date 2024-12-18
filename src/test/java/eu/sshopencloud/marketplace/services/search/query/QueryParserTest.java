package eu.sshopencloud.marketplace.services.search.query;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class QueryParserTest {

    @Test
    public void shouldParseEmptyPhrase() {
        String phrase = "";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(0));
    }

    @Test
    public void shouldParseOneWordPhrase() throws Exception {
        String phrase = "gephi";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(1));
        assertThat(queryParts, contains(new QueryPart("gephi", false)));
    }

    @Test
    public void shouldParseTwoWordPhrase() throws Exception {
        String phrase = "gephi complex+test";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(2));
        assertThat(queryParts, contains(new QueryPart("gephi", false),
                                        new QueryPart("complex\\+test", false)));
    }

    @Test
    public void canQueryURL() throws Exception {
        String phrase = "https://test.eu/test";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(1));
        assertThat(queryParts, contains(new QueryPart("https\\:\\/\\/test.eu\\/test", false)));
    }

    @Test
    public void shouldParseThreeWordsPhrase() throws Exception {
        String phrase = "gephi complex test";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(3));
        assertThat(queryParts, contains(new QueryPart("gephi", false),
                                        new QueryPart("complex", false),
                                        new QueryPart("test", false)));
    }

    @Test
    public void shouldParseOneWordQuotedPhrase() throws Exception {
        String phrase = "\"gephi\"";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(1));
        assertThat(queryParts, contains(new QueryPart("gephi", true)));
    }

    @Test
    public void shouldParseOneQuotedPhrase() throws Exception {
        String phrase = "\"gephi complex\"";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(1));
        assertThat(queryParts, contains(new QueryPart("gephi\\ complex", true)));
    }

    @Test
    public void shouldParseOneQuotedAndTwoWordsPhrase() throws Exception {
        String phrase = "\"gephi complex\" test 2";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(3));
        assertThat(queryParts, contains(new QueryPart("gephi\\ complex", true),
                                        new QueryPart("test", false),
                                        new QueryPart("2", false)));
    }

    @Test
    public void shouldParseOneQuotedAndOneWordsPhrase() throws Exception {
        String phrase = "\"star\\\" complex\" test ";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(2));
        assertThat(queryParts, contains(new QueryPart("star\\\"\\ complex", true),
                new QueryPart("test", false)));
    }

    @Test
    public void shouldParseTwoQuotedPhrase() throws Exception {
        String phrase = "\"gephi complex\" \"test 2";

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        assertThat(queryParts, hasSize(2));
        assertThat(queryParts, contains(new QueryPart("gephi\\ complex", true),
                new QueryPart("test\\ 2", true)));
    }

}
