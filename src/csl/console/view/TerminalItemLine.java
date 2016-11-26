package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.List;

public class TerminalItemLine implements TerminalItem {
    protected int depth;
    protected List<AttributedString> tokens;
    protected TerminalItem parent;

    public TerminalItemLine() {}

    public TerminalItemLine(List<AttributedString> tokens) {
        this(0, tokens, null);
    }

    public TerminalItemLine(int depth, List<AttributedString> tokens, TerminalItem parent) {
        this.depth = depth;
        this.tokens = tokens;
        this.parent = parent;
    }

    /** returns this */
    public TerminalItemLine withParent(TerminalItemLine parent) {
        this.parent = parent;
        this.depth = parent.getDepth() + 1;
        return this;
    }

    public int getDepth() {
        return depth;
    }

    public List<AttributedString> getTokens() {
        return tokens;
    }

    public TerminalItem getParent() {
        return parent;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setTokens(List<AttributedString> tokens) {
        this.tokens = tokens;
    }

    public void setParent(TerminalItem parent) {
        this.parent = parent;
    }
}
