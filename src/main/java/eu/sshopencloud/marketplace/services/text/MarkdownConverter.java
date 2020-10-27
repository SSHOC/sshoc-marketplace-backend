package eu.sshopencloud.marketplace.services.text;

import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;


@UtilityClass
@Slf4j
public class MarkdownConverter {

    public String convertHtmlToMarkdown(String html) {
        String withoutUrlInAngleBrackets = html.replaceAll("<http([^>])*>", "");
        boolean valid = Jsoup.isValid(withoutUrlInAngleBrackets, Whitelist.none());
        if (!valid) {
            // with empty whitelist means that there is at least one tag
            Document htmlDocument = Jsoup.parseBodyFragment(withoutUrlInAngleBrackets);
            Element bodyElement = htmlDocument.body();
            // assume that mixed markup (markdown and html) is not nested
            boolean htmlOnly = (bodyElement.childNodeSize() == 1);
            StringBuilder markdown = new StringBuilder();
            for (int i = 0; i < bodyElement.childNodeSize(); i++) {
                org.jsoup.nodes.Node child = bodyElement.childNode(i);
                if (child instanceof TextNode) {
                    markdown.append(((TextNode) child).getWholeText());
                } else {
                    String part = FlexmarkHtmlConverter.builder().build().convert(child);
                    if (!htmlOnly && part.endsWith("\n")) {
                        part = part.substring(0, part.length() - 1);
                    }
                    markdown.append(part);
                }
            }
            return markdown.toString();
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
