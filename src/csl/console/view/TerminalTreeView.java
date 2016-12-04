package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *  To obtain the instance:
 * <pre>
 *     TerminalTreeView view = new TerminalTreeView(root, tree); //root can be null
 *     view.setHeight(rows);
 *     view.setWidth(columns);
 *
 *     view.setCursorLine(row);
 *     view.setOffsetX(col);
 * </pre>
 *
 *  To change the root item:
 * <pre>
 *     view.setOrigin(newRoot);
 *     view.build();
 * </pre>
 *
 *  To display the lines:
 * <pre>
 *     display.update(view.write().getLines(), size.cursorPos(view.getCursorLine(), 0));
 * </pre>
 *
 *  The class supports scroll operations (scroll...() and move...()).
 *     Those methods will set {@link #needToUpdateDisplay} to true
 *        if succeeded their operations and the list of items or its tokens is changed.
 *         Note: column changes ({@link #scrollToNextColumn()} and {@link #scrollToPreviousColumn()})
 *                will not change them.
 *
 */
public class TerminalTreeView {
    protected TerminalItem origin;
    protected TerminalTree tree;

    protected List<DisplayItem> displayItems;
    protected List<DisplayColumn> displayColumns = new ArrayList<>(3);
    protected int displayScrollableWidth; //virtual column width
    protected int displayFixedWidth;
    protected volatile boolean needToUpdateDisplay = true;

    protected volatile int width = 100;
    protected volatile int height = 30;
    protected volatile boolean needToReBuild = true;

    protected int offsetX;
    protected int offsetY;

    protected int cursorLine;

    public TerminalTreeView(TerminalItem origin, TerminalTree tree) {
        this.origin = origin;
        this.tree = tree;
        displayItems = new ArrayList<>(height);
    }

    public List<DisplayItem> getDisplayItems() {
        return displayItems;
    }
    public TerminalItem getOrigin() {
        return origin;
    }
    public void setOrigin(TerminalItem origin) {
        this.origin = origin;
    }
    public TerminalTree getTree() {
        return tree;
    }
    public void setTree(TerminalTree tree) {
        this.tree = tree;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
        needToUpdateDisplay = true;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        if (this.height != height) {
            this.height = height;
            needToReBuild = true;
        }
    }
    public int getOffsetX() {
        return offsetX;
    }
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }
    public int getOffsetY() {
        return offsetY;
    }
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
    /** may re-build and justify the cursor */
    public int getCursorLine() {
        getDisplayItemsWithBuild(false);
        return cursorLine;
    }
    public void setCursorLine(int cursorLine) {
        this.cursorLine = cursorLine;
        needToUpdateDisplay = true;
    }

    /////////////////////////////////

    public List<DisplayItem> getDisplayItemsWithBuild(boolean requireDisplayTokens) {
        if (needToReBuild) {
            build();
        }
        if (requireDisplayTokens && needToUpdateDisplay) {
            updateDisplayTokens();
        }
        return displayItems;
    }

    public void build() {
        displayItems.clear();
        needToUpdateDisplay = true;

        buildLine(new BuildIndex(0, 0), origin);
        addNextLinesToHeight();
        updateOrigin();
        updateCursorLine();
        needToReBuild = false;
    }

    /**
     * <pre>
     *        idx.dI: existingItem1
     *           +1 : existingItem2
     *       =&gt;
     *        idx.dI: item
     *           +1 : existingItem1
     *           +2 : existingItem2
     *
     *       =&gt;  //call {@link #buildLineChildren(BuildIndex, TerminalItem)}
     *        idx.dI: item
     *           +1 :    getFirstChild(item)
     *           +2 : existingItem1
     *           +3 : existingItem2
     *       =&gt;
     *        idx.dI: item
     *           +1 :    getFirstChild(item)
     *           +2 :       getFirstChild(getFirstChild(item)) //recursive call
     *           +3 : existingItem1
     *           +4 : existingItem2
     *       =&gt;
     *        idx.dI: item
     *           +1 :    getFirstChild(item)
     *           +2 :       getFirstChild(getFirstChild(item))
     *           +3 :    getNextSibling(getFirstChild(item))
     *           +4 : existingItem1
     *           +5 : existingItem2
     * </pre>
     */
    public boolean buildLine(BuildIndex idx, TerminalItem item) {
        if (item == null) {
            return false;
        } else if (idx.y < getDisplayMaxLine()) {
            if (idx.y >= getDisplayMinLine()) {
                displayItems.add(idx.displayIndex, makeDisplayItem(item));
                needToUpdateDisplay = true;
                ++idx.displayIndex;
            }
            ++idx.y;
            return buildLineChildren(idx, item);
        } else {
            return false;
        }
    }
    protected boolean buildLineChildren(BuildIndex idx, TerminalItem item) {
        TerminalItem child = tree.isOpen(item) ? tree.getFirstChild(item) : null;
        boolean result = true;
        while (child != null) {
            if (!buildLine(idx, child)) {
                result = false;
                break;
            }
            child = tree.getNextSibling(child);
        }
        return result;
    }

    /**
     * <pre>
     *          0: firstItem
     *             ...
     *       last: lastItem
     *     last+1:  *empty*
     *             ...
     *     height:  *empty*
     *
     *    =&gt;  //call {@link #addNextLine()}s
     *          0: firstItem
     *             ...
     *       last: lastItem
     *     last+1: getNext(lastItem)
     *             ...
     *     height: getNext(...)
     * </pre>
     */
    protected void addNextLinesToHeight() {
        while (displayItems.size() < height) {
            if (!addNextLine()) {
                break;
            }
        }
    }

    protected boolean addNextLine() {
        DisplayItem last = displayItems.get(displayItems.size() - 1);
        TerminalItem next = tree.getNext(last.getItem());
        if (next != null) {
            displayItems.add(makeDisplayItem(next));
            needToUpdateDisplay = true;
            return true;
        } else {
            return false;
        }
    }


    protected void updateOrigin() {
        if (!displayItems.isEmpty()) {
            origin = displayItems.get(0).getItem();
        }
        offsetY = 0;
    }

    protected void updateCursorLine() {
        if (cursorLine >= height) {
            cursorLine = height - 1;
            needToUpdateDisplay = true;
        } else if (cursorLine < 0) {
            cursorLine = 0;
            needToUpdateDisplay = true;
        }
    }

    protected void updateDisplayTokens() {
        displayColumns.clear();
        int i = 0;
        for (DisplayItem item : displayItems) {
            item.updateTokens(tree, getLineHead(item.getItem(), i == cursorLine));
            updateDisplayMaxWidth(item.getColumnWidth(), item.getColumnIndent());
            ++i;
        }
        updateScrollableColumns();
        needToUpdateDisplay = false;
    }
    protected void updateDisplayMaxWidth(int[] columnWidth, boolean[] columnIndent) {
        while (displayColumns.size() < columnWidth.length) {
            displayColumns.add(new DisplayColumn());
        }
        for (int i = 0, l = columnWidth.length; i < l; ++i) {
            DisplayColumn column = displayColumns.get(i);
            column.updateWidth(columnWidth[i]);
            if (columnIndent != null && i < columnIndent.length) {
                column.updateIndent(columnIndent[i]);
            }
        }
    }
    protected void updateScrollableColumns() {
        int s = 0;
        int e = displayColumns.size() - 1;
        int fixedWidth = 0;
        int limitWidth = width - 10;
        while (s < e) {
            DisplayColumn sc = displayColumns.get(s);
            DisplayColumn ec = displayColumns.get(e);

            boolean selectStart = sc.getWidth() <= ec.getWidth();
            DisplayColumn c = selectStart ? sc : ec;
            if (fixedWidth + c.getWidth() < limitWidth) {
                if (selectStart) {
                    s++;
                } else {
                    e--;
                }
                fixedWidth += c.getWidth();
                c.scrollable = false;
            }
        }
        displayScrollableWidth = displayColumns.stream()
                .filter(DisplayColumn::isScrollable)
                .mapToInt(DisplayColumn::getWidth)
                .sum();
        displayFixedWidth = fixedWidth;
    }

    public static class DisplayColumn {
        public boolean scrollable = true;
        public Boolean indent = null;
        public int width;
        public void updateWidth(int w) {
            if (w > width) {
                width = w;
            }
        }
        public void updateIndent(boolean i) {
            if (indent == null || indent) {
                indent = i;
            }
        }

        public int getWidth() {
            return width;
        }

        public boolean isScrollable() {
            return scrollable;
        }

        public boolean isIndent() {
            return indent == null ? false : indent;
        }

        @Override
        public String toString() {
            return "DC(" + width + ", scroll=" + scrollable + ", indent=" + indent + ")";
        }
    }

    private static List<AttributedString> headCursor = Collections.singletonList(new AttributedString("*"));
    private static List<AttributedString> headSpace = Collections.singletonList(new AttributedString(" "));

    protected List<AttributedString> getLineHead(TerminalItem item, boolean cursorLine) {
        return cursorLine ? headCursor : headSpace;
    }



    /////////////////////////////////

    protected DisplayItem makeDisplayItem(TerminalItem item) {
        return new DisplayItem(item);
    }

    public static class BuildIndex {
        public int y;
        public int displayIndex;

        public BuildIndex(int y, int displayIndex) {
            this.y = y;
            this.displayIndex = displayIndex;
        }
    }

    public static class DisplayItem {
        protected TerminalItem item;
        protected List<List<AttributedString>> itemTokens;
        protected List<List<AttributedString>> columnTokens;
        protected int[] columnWidth = null;
        protected boolean[] columnIndent = null;

        public DisplayItem(TerminalItem item) {
            this.item = item;
        }

        public TerminalItem getItem() {
            return item;
        }

        public void updateTokens(TerminalTree tree, List<AttributedString> head) {
            if (itemTokens == null){
                itemTokens = tree.getColumnTokens(item);
            }
            List<List<AttributedString>> cs = itemTokens;
            ConsoleLogger.log("cols: " + cs + " head: " + head);
            columnTokens = new ArrayList<>(cs.size() + 1);
            if (head != null) {
                columnTokens.add(head);
            }
            columnTokens.addAll(cs);
            columnWidth = columnTokens.stream()
                    .mapToInt(c -> c.stream()
                            .mapToInt(AttributedString::columnLength)
                            .sum())
                    .toArray();

            columnIndent = tree.getColumnTokenIndents(item, columnTokens);
        }

        public List<List<AttributedString>> getColumnTokens() {
            return columnTokens;
        }

        public int[] getColumnWidth() {
            return columnWidth;
        }

        public boolean[] getColumnIndent() {
            return columnIndent;
        }

        @Override
        public String toString() {
            return "DI(" + item + ", colWidth=" + Arrays.toString(columnWidth) + ")";
        }
    }

    /////////////////////////////////

    /** exclusive */
    public int getDisplayMaxLine() {
        return offsetY + height;
    }
    /** inclusive */
    public int getDisplayMinLine() {
        return offsetY;
    }


    /////////////////////////////////

    /**
     * <pre>
     *      0: firstItem
     *      1: secondItem
     *         ...
     *      h: lastItem
     *     =&gt;
     *      //firstItem is out
     *      0: secondItem  //origin
     *         ...
     *    h-1: lastItem
     *      h: getNext(lastItem) //{@link #addNextLine()}
     * </pre>
     */
    public boolean scrollToNextLine() {
        boolean result = false;
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        if (displayItems.size() >= height) {
            DisplayItem removed = displayItems.remove(0);
            if (!addNextLine()) {
                displayItems.add(0, removed); //cancel
                return false;
            }
            result = true;
        }
        updateOrigin();
        if (result) {
            needToUpdateDisplay = true;
        }
        return result;
    }

    /**
     * <pre>
     *      0: firstItem
     *         ...
     *         lastPrevItem
     *      h: lastItem
     *     =&gt;
     *      0: getPrevious(firstItem)  //origin
     *      1: firstItem
     *         ...
     *      h: lastPrevItem
     *      // lastItem is out
     * </pre>
     */
    public boolean scrollToPreviousLine() {
        DisplayItem removed = null;
        boolean result = false;
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        if (displayItems.size() >= height) {
            removed = displayItems.remove(displayItems.size() - 1);
        }

        if (!displayItems.isEmpty()) {
            DisplayItem top = displayItems.get(0);
            TerminalItem prev = tree.getPrevious(top.getItem());
            if (prev != null) {
                displayItems.add(0, makeDisplayItem(prev));
                result = true;
            } else if (removed != null) { //cancel
                displayItems.add(removed);
            }
        }
        updateOrigin();
        if (result) {
            needToUpdateDisplay = true;
        }
        return result;
    }

    public void scrollToNextColumn() {
        getDisplayItemsWithBuild(true); //to calculate the scrollable width
        if (offsetX + width < displayScrollableWidth) {
            ++offsetX;
        }
    }

    public void scrollToPreviousColumn() {
        if (offsetX > 0) {
            --offsetX;
        }
    }

    /////////////////////////////////

    /** a combination of {@link #makeWriting()} and
     *  {@link #writeLine(TerminalLineColumnsWriting, DisplayItem)}*/
    public TerminalLineColumnsWriting write() {
        TerminalLineColumnsWriting writing = makeWriting();
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(true);
        displayItems.forEach(item ->
                writeLine(writing, item));
        return writing;
    }

    protected TerminalLineColumnsWriting makeWriting() {
        return new TerminalLineColumnsWriting(width, height);
    }

    public void writeLine(TerminalLineColumnsWriting writing, DisplayItem item) {
        boolean scrollable = false; //[fxd],[fxd]...[fxd],[scr,scr...scr],[fxd],[fxd]...[fdx]
        List<List<AttributedString>> columnTokens = item.getColumnTokens();

        DisplayColumn prevColumn = null;
        for (int i = 0, l = columnTokens.size(); i < l; ++i) {
            DisplayColumn column = displayColumns.get(i);
            if (!scrollable) {
                if (column.isScrollable()) { //start of scrollable
                    if (prevColumn != null && !prevColumn.isIndent()) {
                        writing.appendSpace(writing.getLineColumnRemaining());
                    }
                    writing.nextColumn(offsetX, displayScrollableWidth);
                    scrollable = true;
                } else {
                    if (prevColumn != null && !prevColumn.isIndent()) {
                        writing.appendSpace(writing.getLineColumnRemaining());
                    }
                    writing.nextColumn(0, column.getWidth());
                }
            } else {
                if (!column.isScrollable()) { //end of scrollable
                    if (prevColumn != null && !prevColumn.isIndent()) {
                        writing.appendSpace(writing.getLineColumnRemaining());
                    }
                    writing.nextColumn(0, column.getWidth());
                    scrollable = false;
                }
            }
            columnTokens.get(i).forEach(writing::append);
            prevColumn = column;
        }
        writeLineEnd(writing);
    }


    public void writeLineEnd(TerminalLineColumnsWriting writing) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(true);
        writing.nextLine(writing.getLineY() + 1 >= displayItems.size());
    }


    /////////////////////////////////

    public void scrollToNextLineWithCursor() {
        if (cursorLine + 1 >= height) {
            scrollToNextLine();
        } else {
            List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
            if (cursorLine < displayItems.size()) {
                cursorLine++;
                needToUpdateDisplay = true;
            }
        }
    }

    public void scrollToPreviousLineWithCursor() {
        if (cursorLine <= 0) {
            scrollToPreviousLine();
        } else {
            --cursorLine;
            needToUpdateDisplay = true;
        }
    }

    public void openOnCursor(boolean open) {
        int idx = getCursorLine();
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        if (idx >= 0 && idx < displayItems.size()) {
            openDisplayItem(idx, open);
        }
    }
    protected TerminalItem openDisplayItem(int idx, boolean open) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        DisplayItem displayItem = displayItems.get(idx);
        TerminalItem item = (open ? tree.open(displayItem.getItem()) : tree.close(displayItem.getItem()));

        List<DisplayItem> old = displayItems;
        displayItems = new ArrayList<>(height);
        this.displayItems = displayItems;
        needToUpdateDisplay = true;
        displayItems.addAll(old.subList(0, idx));

        BuildIndex buildIndex = new BuildIndex(getDisplayMinLine() + idx, idx);
        buildLine(buildIndex, item);

        reuseRestItems(item, old);

        addNextLinesToHeight();
        return item;
    }
    protected void reuseRestItems(TerminalItem item, List<DisplayItem> old) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        if (item == null) {
            return;
        }
        TerminalItem next = tree.getUpperNext(item);
        if (next == null) {
            return;
        }
        boolean afterNext = false;
        for (int i = getCursorLine() + 1, e = old.size(); i < e; ++i) {
            if (displayItems.size() >= height) {
                break;
            }
            DisplayItem rest = old.get(i);
            if (afterNext || rest.getItem().equals(next)) {
                afterNext = true;
                displayItems.add(rest);
                needToUpdateDisplay = true;
            }
        }
    }


    public TerminalItem open(TerminalItem item, boolean open) {
        int idx = getDisplayedItemIndex(item);
        if (idx >= 0) {
            return openDisplayItem(idx, open);
        } else {
            if (open) {
                return tree.open(item);
            } else {
                return tree.close(item);
            }
        }
    }
    public int getDisplayedItemIndex(TerminalItem item) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        int line = 0;
        for (DisplayItem displayItem : displayItems) {
            if (displayItem.getItem().equals(item)) {
                return line;
            }
            ++line;
        }
        return -1;
    }

    public DisplayItem getDisplayItemOnCursor() {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);
        int idx = getCursorLine();
        if (idx >=0 && idx < displayItems.size()) {
            return displayItems.get(idx);
        } else {
            return null;
        }
    }

    public TerminalItem getItemOnCursor() {
        DisplayItem item = getDisplayItemOnCursor();
        if (item != null) {
            return item.getItem();
        } else {
            return null;
        }
    }


    public void moveCursorTo(TerminalItem item) {
        if (item == null) {
            return;
        }
        List<DisplayItem> displayItems = getDisplayItemsWithBuild(false);

        //an item in displayed items
        int line = getDisplayedItemIndex(item);
        if (line != -1) {
            cursorLine = line;
            needToUpdateDisplay = true;
            return;
        }

        //reconstruct
        //e.g. height=10, cursorLine=6 => [0,1,2,3,4,5,6],7,8,9
        TerminalItem last = item;
        displayItems.clear();
        needToUpdateDisplay = true;
        for (int i = 0, n = cursorLine; i <= n; ++i) {
            if (last != null) {
                displayItems.add(0, makeDisplayItem(last));
                last = tree.getPrevious(last);
            } else {
                break;
            }
        }

        addNextLinesToHeight();
        updateOrigin();
    }

    ////////////////////////


    public void openOrCloseOnCursor() {
        TerminalItem item = getItemOnCursor();
        if (item != null) {
            openOnCursor(!tree.isOpen(item));
        }
    }

    public void scrollDownPage() {
        for (int i = 0, l = getHeight() / 2; i < l; ++i) {
            if (!scrollToNextLine()) {
                break;
            }
        }
    }

    public void scrollUpPage() {
        for (int i = 0, l = getHeight() / 2; i < l; ++i) {
            if (!scrollToPreviousLine()) {
                break;
            }
        }
    }

    public void moveToParent() {
        TerminalItem item = getItemOnCursor();
        if (item != null) {
            moveCursorTo(tree.getParent(item));
        }
    }

    public void moveToFirstChild() {
        TerminalItem item = getItemOnCursor();
        if (item != null) {
            if (!tree.isOpen(item)) {
                item = open(item, true);
            }
            TerminalItem first = tree.getFirstChild(item);
            if (first != null) {
                moveCursorTo(first);
            }
        }
    }

    public void moveToLastChild() {
        TerminalItem item = getItemOnCursor();
        if (item != null) {
            if (!tree.isOpen(item)) {
                item = open(item, true);
            }
            TerminalItem last = tree.getLastChild(item);
            if (last != null) {
                moveCursorTo(last);
            }
        }
    }

    public void moveToPreviousSibling() {
        TerminalItem item = getItemOnCursor();
        if (item != null) {
            moveCursorTo(tree.getUpperPrevious(item));
        }
    }

    public void moveToNextSibling() {
        TerminalItem item = getItemOnCursor();
        if (item != null) {
            moveCursorTo(tree.getUpperNext(item));
        }
    }

    public void debugLog() {
        int i = 0;
        ConsoleLogger.log("w=" + width + ",h=" + height +
                ", off=(" + offsetX + "," + offsetY + ") " +
                ", cursorLine=" + cursorLine);

        StringBuilder buf = new StringBuilder();
        buf.append("column: ");
        for (DisplayColumn c : displayColumns) {
            buf.append(" [").append(i).append("] ").append(c);
            ++i;
        }
        ConsoleLogger.log(buf.toString());

        i = 0;
        for (DisplayItem item : displayItems) {
            ConsoleLogger.log(i + " : " + item.toString() +
                    (i == cursorLine ? " : cursorLine" : "") +
                    "  " + item.getColumnTokens());
            ++i;
        }
    }
}
