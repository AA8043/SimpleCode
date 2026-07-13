package org.a8043.simpleCode.frontend;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.HashSet;
import java.util.Set;

public class MarkdownToBBCodeRenderer {
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownToBBCodeRenderer() {
        MutableDataHolder options = new MutableDataSet();
        BBCodeNodeRendererFactory nodeRendererFactory = new BBCodeNodeRendererFactory();
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options)
            .nodeRendererFactory(nodeRendererFactory)
            .build();
    }

    public String convert(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    private static class BBCodeNodeRendererFactory implements NodeRendererFactory {
        @Override
        public NodeRenderer apply(DataHolder options) {
            return new BBCodeNodeRenderer();
        }
    }

    private static class BBCodeNodeRenderer implements NodeRenderer {
        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            Set<NodeRenderingHandler<?>> handlers = new HashSet<>();

            handlers.add(new NodeRenderingHandler<>(StrongEmphasis.class,
                (node, context, html) -> write(node, context, "[b]", "[/b]")));

            handlers.add(new NodeRenderingHandler<>(Emphasis.class,
                (node, context, html) -> write(node, context, "[i]", "[/i]")));

            handlers.add(new NodeRenderingHandler<>(Strikethrough.class,
                (node, context, html) -> write(node, context, "[s]", "[/s]")));

            handlers.add(new NodeRenderingHandler<>(Code.class,
                (node, context, html) -> {
                    html.raw("[code]");
                    html.text(node.getText());
                    html.raw("[/code]");
                }));

            handlers.add(new NodeRenderingHandler<>(Link.class,
                (node, context, html) -> {
                    html.raw("[url=" + node.getUrl() + "]");
                    context.renderChildren(node);
                    html.raw("[/url]");
                }));

            handlers.add(new NodeRenderingHandler<>(Image.class, (node, context, html) ->
                html.raw("[img]" + node.getUrl() + "[/img]")));

            handlers.add(new NodeRenderingHandler<>(BulletList.class,
                (node, context, html) -> {
                    html.raw("[list]");
                    context.renderChildren(node);
                    html.raw("[/list]");
                }));

            handlers.add(new NodeRenderingHandler<>(OrderedList.class,
                (node, context, html) -> {
                    html.raw("[list=1]");
                    context.renderChildren(node);
                    html.raw("[/list]");
                }));

            handlers.add(new NodeRenderingHandler<>(BulletListItem.class,
                (node, context, html) -> {
                    html.raw("[*]");
                    context.renderChildren(node);
                }));

            handlers.add(new NodeRenderingHandler<>(OrderedListItem.class,
                (node, context, html) -> {
                    html.raw("[*]");
                    context.renderChildren(node);
                }));

            handlers.add(new NodeRenderingHandler<>(BlockQuote.class,
                (node, context, html) -> {
                    html.raw("[quote]");
                    context.renderChildren(node);
                    html.raw("[/quote]");
                }));

            handlers.add(new NodeRenderingHandler<>(Heading.class,
                (node, context, html) -> {
                    html.raw("[b]");
                    context.renderChildren(node);
                    html.raw("[/b]\n");
                }));

            handlers.add(new NodeRenderingHandler<>(Paragraph.class,
                (node, context, html) -> {
                    context.renderChildren(node);
                    html.raw("\n");
                }));

            handlers.add(new NodeRenderingHandler<>(SoftLineBreak.class, (node, context, html) -> html.raw("\n")));

            handlers.add(new NodeRenderingHandler<>(HardLineBreak.class, (node, context, html) -> html.raw("\n")));

            handlers.add(new NodeRenderingHandler<>(FencedCodeBlock.class,
                (node, context, html) -> {
                    if (node.getInfo() != null && !node.getInfo().isEmpty()) {
                        html.raw("[code=" + node.getInfo() + "]");
                    } else {
                        html.raw("[code]");
                    }
                    html.raw(node.getContentChars().toString());
                    html.raw("[/code]\n");
                }));

            handlers.add(new NodeRenderingHandler<>(ThematicBreak.class,
                (node, context, html) -> html.raw("[hr]\n")));

            return handlers;
        }
    }

    private static void write(Node node, NodeRendererContext context, String openTag, String closeTag) {
        context.getHtmlWriter().raw(openTag);
        context.renderChildren(node);
        context.getHtmlWriter().raw(closeTag);
    }
}