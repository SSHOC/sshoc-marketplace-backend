package eu.sshopencloud.marketplace.services.text;

import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

@UtilityClass
@Slf4j
public class MarkdownConverter {

    public String convertHtmlToMarkdown(String html) {
        String withoutUrlInAngleBrackets = html.replaceAll("<http([^>])*>", "");
        boolean valid = Jsoup.isValid(withoutUrlInAngleBrackets, Whitelist.none());
        if (!valid) {
            // with empty whitelist means that there is at least one tag
            return FlexmarkHtmlConverter.builder().build().convert(html);
        } else {
            // no html tag exists
            return html; // which is actually markdown
        }
    }


    public String convertMarkdownToText(String markdown) {
        DataHolder options = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
        Node document = Parser.builder(options).build().parse(markdown);
        return new TextCollectingVisitor().collectAndGetText(document);
    }

}
