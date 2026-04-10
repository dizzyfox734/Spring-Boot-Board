package dizzyfox734.springbootboard.global.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

@Component
public class MarkdownUtil {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .build();

    public String markdown(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
