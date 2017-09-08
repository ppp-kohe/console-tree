package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TerminalItemNode extends TerminalItemLine {
    protected List<TerminalItem> children;

    public TerminalItemNode() {}

    public TerminalItemNode(List<List<AttributedString>> columnTokens, List<TerminalItem> children) {
        super(columnTokens);
        withChildren(children);
    }

    public TerminalItemNode(List<TerminalItem> children) {
        this();
        withChildren(children);
    }

    @Override
    public TerminalItemNode withParent(TerminalItemLine parent) {
        super.withParent(parent);
        return this;
    }

    @Override
    public TerminalItemNode withColumnTokens(List<List<AttributedString>> columnTokens) {
        super.withColumnTokens(columnTokens);
        return this;
    }

    @Override
    public TerminalItemNode withInfoLines(List<AttributedString> infoLines) {
        super.withInfoLines(infoLines);
        return this;
    }

    public TerminalItemNode withChildren(List<TerminalItem> children) {
        this.children = children;
        if (children != null) {
            children.forEach(this::setItemAsChild);
        }
        return this;
    }

    /**
     * a subclass can overrides the method for custom child retrieving
     * e.g.
     * <pre>
     *     public List&lt;TerminalItem&gt; getChildren() {
     *         if (this.children == null) {
     *             withChildren(...code for constructing a list of children...);
     *         }
     *         return this.children;
     *     }
     * </pre>
     *  You can construct the children by {@link #addChild(TerminalItem)}, {@link #addChildren(List)},
     *     {@link #withChildren(List)}, and {@link #setItemAsChild(TerminalItem)}.
     *     Those methods eventually call {@link #setItemAsChild(TerminalItem)},
     *        which sets the parent of the given child to this node,
     *        only if the child is a {@link TerminalItemLine}.
     */
    public List<TerminalItem> getChildren() {
        return children;
    }

    /** returns item */
    public TerminalItem addChild(TerminalItem item) {
        setItemAsChild(item);
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(item);
        return item;
    }

    public void setItemAsChild(TerminalItem item) {
        if (item instanceof TerminalItemLine) {
            TerminalItemLine line = (TerminalItemLine) item;
            line.withParent(this);
        }
    }

    public List<? extends TerminalItem> addChildren(List<? extends TerminalItem> items) {
        for (TerminalItem i : items) {
            addChild(i);
        }
        return items;
    }

    @Override
    public String toString() {
        return "Node(" + toStringContents() + ", children=" +
                Optional.ofNullable(getChildren())
                        .map(List::size)
                        .orElse(0) + ")";
    }
}
