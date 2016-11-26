package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;

public class TerminalItemNode extends TerminalItemLine {
    protected List<TerminalItem> children;

    public TerminalItemNode() {}

    public TerminalItemNode(List<AttributedString> tokens, List<TerminalItem> children) {
        super(tokens);
        this.children = children;
    }

    public void setChildren(List<TerminalItem> children) {
        this.children = children;
    }

    public List<TerminalItem> getChildren() {
        return children;
    }

    /** returns item */
    public TerminalItem addChild(TerminalItem item) {
        if (item instanceof TerminalItemLine) {
            TerminalItemLine line = (TerminalItemLine) item;
            line.withParent(this);
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(item);
        return item;
    }

    public List<? extends TerminalItem> addChildren(List<? extends TerminalItem> items) {
        for (TerminalItem i : items) {
            addChild(i);
        }
        return items;
    }

}
