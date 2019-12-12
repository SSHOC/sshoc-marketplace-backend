package eu.sshopencloud.marketplace.services.text;

import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MarkdownConverter {

    public String convertHtmlToMarkdown(String html) {
        // TODO options (?)
        return FlexmarkHtmlConverter.builder().build().convert(html);
    }

    public String convertMarkdownToText(String markdown) {
        DataHolder options = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
        Node document = Parser.builder(options).build().parse(markdown);
        return new TextCollectingVisitor().collectAndGetText(document);
    }

}
