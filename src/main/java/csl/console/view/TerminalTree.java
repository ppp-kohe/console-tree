package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.List;

public interface TerminalTree {

    List<List<AttributedString>> getColumnTokens(TerminalItem item);
    /**
     * returns whether each column is an indent or not.
     * <p>
     * columnTokens == getColumnTokens(item) and
     *   columnTokens.size() == getColumnTokenIndents(item, columnTokens).length */
    boolean[] getColumnTokenIndents(TerminalItem item, List<List<AttributedString>> columnTokens);

    List<AttributedString> getInfoLines(TerminalItem item);


    TerminalItem open(TerminalItem item);
    TerminalItem close(TerminalItem item);

    boolean isOpen(TerminalItem item);

    TerminalItem getParent(TerminalItem item);
    /** optional */
    List<TerminalItem> getChildren(TerminalItem item);

    TerminalItem getFirstChild(TerminalItem item);
    TerminalItem getLastChild(TerminalItem item);

    TerminalItem getNextSibling(TerminalItem item);
    TerminalItem getPreviousSibling(TerminalItem item);

    TerminalItem getNext(TerminalItem item);
    TerminalItem getPrevious(TerminalItem item);

    TerminalItem getUpperNext(TerminalItem item);
    TerminalItem getUpperPrevious(TerminalItem item);
}
