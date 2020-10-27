package eu.sshopencloud.marketplace.services.text;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarkdownConverterTest {

    @Test
    public void shouldConvertSimpleHtmlToMarkdown() throws Exception {
        String html = "<div>Description</div>";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(html);

        assertThat(markdown, is("Description\n"));
    }

    @Test
    public void shouldConvertComplexHtmlToMarkdown() throws Exception {
        String html = "<div>Description\n"
                + "  <p>Lorem ipsum <code>class</code> <i>Ctrl</i> <strong>Alt</strong> <a href='http://example.com'>link</a></p>\n"
                + "  <ul>\n"
                + "    <li>Item 1</li>\n"
                + "    <li>Item 2</li>\n"
                + "  </ul>\n"
                + "  <table>\n"
                + "    <thead>\n"
                + "      <tr><th> Element</th><th>Abbreviation</th><th>Expansion</th></tr>\n"
                + "    </thead>\n"
                + "    <tbody>\n"
                + "      <tr><td>Abbreviation</td><td><code>.abbreviation</code></td><td><code>*[]:</code></td></tr>\n"
                + "      <tr><td>Code fence</td><td><code>.codefence</code></td><td>``` ... ```</td></tr>\n"
                + "      <tr><td>Explicit link</td><td><code>.link</code></td><td><code>[]()</code></td></tr>\n"
                + "    </tbody>\n"
                + "  </table>\n"
                + "</div>";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(html);

        assertThat(markdown, is("Description\n"
                + "\n"
                + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                + "\n"
                + "* Item 1\n"
                + "* Item 2\n"
                + "\n"
                + "|    Element    |  Abbreviation   |     Expansion     |\n"
                + "|---------------|-----------------|-------------------|\n"
                + "| Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                + "| Code fence    | `.codefence`    | \\`\\`\\` ... \\`\\`\\` |\n"
                + "| Explicit link | `.link`         | `[]()`            |\n"));
    }

    @Test
    public void shouldRemainMarkdownWithUrlIntact() throws Exception {
        String source = "Click http://example.com/ *here*";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(source);

        assertThat(markdown, is("Click http://example.com/ *here*"));
    }

    @Test
    public void shouldRemainMarkdownWithJsIntact() throws Exception {
        String source = "# Heading\n"
                + "\n"
                + "Some **bold** text.\n"
                + "\n"
                + "```js\n"
                + "function hello() {}\n"
                + "```\n";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(source);

        assertThat(markdown, is("# Heading\n"
                + "\n"
                + "Some **bold** text.\n"
                + "\n"
                + "```js\n"
                + "function hello() {}\n"
                + "```\n"));
    }


    @Test
    public void shouldConvertHtmlAndRemainMarkdownIntact() throws Exception {
        String source = "# Heading\n"
                + "\n"
                + "Some <strong>bold</strong> text.\n"
                + "\n"
                + "```js\n"
                + "function hello() {}\n"
                + "```\n";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(source);

        assertThat(markdown, is("# Heading\n"
                + "\n"
                + "Some **bold** text.\n"
                + "\n"
                + "```js\n"
                + "function hello() {}\n"
                + "```\n"));
    }


    @Test
    public void shouldRemainComplexMarkdownIntact() throws Exception {
        String source = "Description\n"
                + "\n"
                + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                + "\n"
                + "* Item 1\n"
                + "* Item 2\n"
                + "\n"
                + "|    Element    |  Abbreviation   |     Expansion     |\n"
                + "|---------------|-----------------|-------------------|\n"
                + "| Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                + "| Code fence    | `.codefence`    | ``` ... ```       |\n"
                + "| Explicit link | `.link`         | `[]()`            |\n"
                + "\n";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(source);

        assertThat(markdown, is("Description\n"
                + "\n"
                + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                + "\n"
                + "* Item 1\n"
                + "* Item 2\n"
                + "\n"
                + "|    Element    |  Abbreviation   |     Expansion     |\n"
                + "|---------------|-----------------|-------------------|\n"
                + "| Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                + "| Code fence    | `.codefence`    | ``` ... ```       |\n"
                + "| Explicit link | `.link`         | `[]()`            |\n"
                + "\n"));
    }

    @Test
    public void shouldRemainComplexMarkdownWithAngleBracketsIntact() throws Exception {
        String source = "Description <http://example.com>\n"
                + "\n"
                + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                + "\n"
                + "* Item 1\n"
                + "* Item 2\n"
                + "\n"
                + "|    Element    |  Abbreviation   |     Expansion     |\n"
                + "|---------------|-----------------|-------------------|\n"
                + "| Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                + "| Code fence    | `.codefence`    | ``` ... ```       |\n"
                + "| Explicit link | `.link`         | `[]()`            |\n"
                + "\n";

        String markdown = MarkdownConverter.convertHtmlToMarkdown(source);

        assertThat(markdown, is("Description <http://example.com>\n"
                + "\n"
                + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                + "\n"
                + "* Item 1\n"
                + "* Item 2\n"
                + "\n"
                + "|    Element    |  Abbreviation   |     Expansion     |\n"
                + "|---------------|-----------------|-------------------|\n"
                + "| Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                + "| Code fence    | `.codefence`    | ``` ... ```       |\n"
                + "| Explicit link | `.link`         | `[]()`            |\n"
                + "\n"));
    }

    @Test
    public void shouldConvertMarkdownToText() throws Exception {
        String markdown = "Description\n"
                + "\n"
                + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                + "\n"
                + "* Item 1\n"
                + "* Item 2\n"
                + "\n"
                + "|    Element    |  Abbreviation   |     Expansion     |\n"
                + "|---------------|-----------------|-------------------|\n"
                + "| Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                + "| Code fence    | `.codefence`    | ``` ... ```       |\n"
                + "| Explicit link | `.link`         | `[]()`            |\n"
                + "\n";

        String text = MarkdownConverter.convertMarkdownToText(markdown);

        assertThat(text, is("Description\n"
                + "\n"
                + "Lorem ipsum class Ctrl Alt link\n"
                + "\n"
                + "Item 1\n"
                + "\n"
                + "Item 2    Element      Abbreviation        Expansion      Abbreviation   .abbreviation  *[]:             Code fence     .codefence      ...         Explicit link  .link          []()            "));
    }

}
