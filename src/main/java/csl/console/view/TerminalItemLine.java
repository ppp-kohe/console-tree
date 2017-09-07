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
    protected List<AttributedString> infoLines;
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

    /** returns this */
    public TerminalItemLine withInfoLines(List<AttributedString> infoLines) {
        this.infoLines = infoLines;
        return this;
    }

    /** [a, b, c] -> [[a, b, c]] */
    public static List<List<AttributedString>> toSingleColumn(List<AttributedString> strs) {
        return Collections.singletonList(strs);
    }
    /** [a, b, c] -> [[a, b, c]] */
    public static List<List<AttributedString>> toSingleColumnFromStrings(List<String> strs) {
        return toSingleColumn(strs.stream()
                .map(s -> new AttributedStringBuilder().append(s).toAttributedString())
                .collect(Collectors.toList()));
    }
    /** [a, b, c] -> [[a, b, c]] */
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

    /** [line1, line2\nline3, line4] -> [line1, lin2, line3, line4] */
    public static List<AttributedString> toLines(List<String> srcs) {
        return srcs.stream()
                .flatMap(str -> Arrays.stream(str.split("\\n")))
                .map(l -> new AttributedStringBuilder().append(l).toAttributedString())
                .collect(Collectors.toList());
    }
    /** [line1, line2\nline3, line4] -> [line1, lin2, line3, line4] */
    public static List<AttributedString> toLines(String... strs) {
        return toLines(Arrays.asList(strs));
    }

    /** [col1, col2] -> [col1col2] */
    public static List<AttributedString> toSingleLine(List<String> srcs) {
        String line = srcs.stream()
                .reduce("", (p,n) -> (p + n.replace('\n', ' ')));
        return Collections.singletonList(new AttributedStringBuilder()
                .append(line).toAttributedString());
    }

    /** [col1, col2] -> [col1col2] */
    public static List<AttributedString> toSingleLine(String... srcs) {
        return toSingleLine(Arrays.asList(srcs));
    }

    public int getDepth() {
        return depth;
    }

    /**
     * a subclass can override the method in order to lazily construct the tokens:
     * <pre>
     *      if (this.columnTokens == null) {
     *          this.columnTokens = toSingleStringColumnsFromStrings("col1", "col2");
     *      }
     *      return this.columnTokens;
     * </pre>
     *  Families of {@link #toSingleColumn(List)} and {@link #toSingleStringColumns(List)}
     *    are useful for the construction.
     */
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

    /**
     * a subclass can override the method in order to lazily construct the lines:
     * e.g.
     * <pre>
     *     if (this.infoLines == null) {
     *         this.infoLines = toLines("line1", "line2);
     *     }
     *     return this.infoLines;
     * </pre>
     * Families of {@link #toLines(List)} and {@link #toSingleLine(List)} are useful for the construction.
     */
    public List<AttributedString> getInfoLines() {
        return infoLines;
    }
}
