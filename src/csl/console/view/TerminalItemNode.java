package csl.console.view;

import org.jline.utils.AttributedString;

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

    public void addChild(TerminalItem item) {
        if (item instanceof TerminalItemLine) {
            TerminalItemLine line = (TerminalItemLine) item;
            line.setParent(this);
            line.setDepth(getDepth() + 1);
        }
        children.add(item);
    }

    public void addChildren(List<? extends TerminalItem> items) {
        for (TerminalItem i : items) {
            addChild(i);
        }
    }

}
