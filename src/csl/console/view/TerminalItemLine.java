package csl.console.view;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TerminalItemLine implements TerminalItem {
    protected int depth;
    protected List<List<AttributedString>> columnTokens;
    protected TerminalItem parent;

    public TerminalItemLine() {}

    public TerminalItemLine(List<List<AttributedString>> columnTokens) {
        this(0, columnTokens, null);
    }

    public TerminalItemLine(int depth, List<List<AttributedString>> columnTokens, TerminalItem parent) {
        this.depth = depth;
        this.columnTokens = columnTokens;
        this.parent = parent;
    }

    /** returns this */
    public TerminalItemLine withParent(TerminalItemLine parent) {
        this.parent = parent;
        this.depth = parent.getDepth() + 1;
        return this;
    }

    /** returns this */
    public TerminalItemLine withColumnTokens(List<List<AttributedString>> columnTokens) {
        this.columnTokens = columnTokens;
        return this;
    }

    /** [a, b, c] -> [[a, b, c] */
    public static List<List<AttributedString>> toSingleColumn(List<AttributedString> strs) {
        return Collections.singletonList(strs);
    }
    /** [a, b, c] -> [[a, b, c] */
    public static List<List<AttributedString>> toSingleColumnFromStrings(List<String> strs) {
        return toSingleColumn(strs.stream()
                .map(s -> new AttributedStringBuilder().append(s).toAttributedString())
                .collect(Collectors.toList()));
    }
    /** [a, b, c] -> [[a, b, c] */
    public static List<List<AttributedString>> toSingleColumnFromStrings(String... str) {
        return toSingleColumnFromStrings(Arrays.stream(str)
                .collect(Collectors.toList()));
    }

    /** [a, b, c] -> [[a], [b], [c]] */
    public static List<List<AttributedString>> toSingleStringColumns(List<AttributedString> columns) {
        return columns.stream()
                .map(Collections::singletonList)
                .collect(Collectors.toList());
    }
    /** [a, b, c] -> [[a], [b], [c]] */
    public static List<List<AttributedString>> toSingleStringColumnsFromStrings(List<String> columns) {
        return toSingleStringColumns(columns.stream()
                .map(s -> new AttributedStringBuilder().append(s).toAttributedString())
                .collect(Collectors.toList()));
    }
    /** [a, b, c] -> [[a], [b], [c]] */
    public static List<List<AttributedString>> toSingleStringColumnsFromStrings(String... columns) {
        return toSingleStringColumnsFromStrings(Arrays.asList(columns));
    }

    public int getDepth() {
        return depth;
    }

    public List<List<AttributedString>> getColumnTokens() {
        return columnTokens;
    }

    public TerminalItem getParent() {
        return parent;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }


    public void setParent(TerminalItem parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Line(" + toStringContents() + ")";
    }

    public String toStringContents() {
        return "depth=" + getDepth() + ", tokens=" + getColumnTokens();
    }
}
