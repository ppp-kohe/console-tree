package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;

public class TerminalTreeView {
    protected TerminalItem origin;
    protected TerminalTree tree;

    protected List<DisplayItem> displayItems;
    protected int displayMaxWidth;

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

    public int getDisplayMaxWidth() {
        return displayMaxWidth;
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
        getDisplayItemsWithBuild();
        return cursorLine;
    }
    public void setCursorLine(int cursorLine) {
        this.cursorLine = cursorLine;
    }

    /////////////////////////////////

    public List<DisplayItem> getDisplayItemsWithBuild() {
        if (needToReBuild) {
            build();
        }
        return displayItems;
    }

    public void build() {
        displayItems.clear();
        buildLine(new BuildIndex(0, 0), origin);
        addNextLinesToHeight();
        updateOrigin();
        updateDisplayTokens();
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
        updateDisplayTokens();
    }

    protected boolean addNextLine() {
        DisplayItem last = displayItems.get(displayItems.size() - 1);
        TerminalItem next = tree.getNext(last.getItem());
        if (next != null) {
            displayItems.add(makeDisplayItem(next));
            return true;
        } else {
            return false;
        }
    }

    protected void updateDisplayTokens() {
        displayMaxWidth = 0;
        for (DisplayItem item : displayItems) {
            int w = getDisplayItemWidth(item);
            if (displayMaxWidth < w) {
                displayMaxWidth = w;
            }
        }
    }

    protected int getDisplayItemWidth(DisplayItem item) {
        return getLineHeadWidth() + item.getMaxWidth(tree);
    }

    protected int getLineHeadWidth() {
        return 1;
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
        } else if (cursorLine < 0) {
            cursorLine = 0;
        }
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
        protected List<AttributedString> tokens;
        protected int maxWidth = -1;

        public DisplayItem(TerminalItem item) {
            this.item = item;
        }

        public TerminalItem getItem() {
            return item;
        }

        public List<AttributedString> getTokens(TerminalTree tree) {
            if (tokens == null) {
                tokens = tree.getTokens(item);
            }
            return tokens;
        }

        public int getMaxWidth(TerminalTree tree) {
            if (maxWidth < 0) {
                maxWidth = getTokens(tree).stream()
                        .mapToInt(AttributedString::columnLength)
                        .sum();
            }
            return maxWidth;
        }

        @Override
        public String toString() {
            return "DI(" + item + ", maxWidth=" + maxWidth + ")";
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
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
        if (displayItems.size() >= height) {
            DisplayItem removed = displayItems.remove(0);
            if (!addNextLine()) {
                displayItems.add(0, removed); //cancel
                return false;
            }
            result = true;
        }
        updateOrigin();
        updateDisplayTokens();
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
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
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
        updateDisplayTokens();
        return result;
    }

    public void scrollToNextColumn() {
        if (offsetX + width < displayMaxWidth) {
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
     *  {@link #writeLine(TerminalLineColumnsWriting, boolean, DisplayItem)}*/
    public TerminalLineColumnsWriting write() {
        TerminalLineColumnsWriting writing = makeWriting();
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
        displayItems.forEach(item ->
                writeLine(writing, writing.getLineY() == cursorLine, item));
        return writing;
    }

    protected TerminalLineColumnsWriting makeWriting() {
        return new TerminalLineColumnsWriting(width, height);
    }

    public void writeLine(TerminalLineColumnsWriting writing, boolean cursorLine, DisplayItem item) {
        writeLine(writing, cursorLine, item.getTokens(tree));
    }

    public void writeLine(TerminalLineColumnsWriting writing, boolean cursorLine, List<AttributedString> tokens) {
        writeLineHead(writing, cursorLine);
        writing.nextColumn(offsetX);
        tokens.forEach(writing::append);
        writeLineEnd(writing);
    }

    public void writeLineHead(TerminalLineColumnsWriting writing, boolean cursorLine) {
        writing.nextColumn(0, getLineHeadWidth());
        writing.append(cursorLine ? "*" : " ");
        writing.appendSpace(writing.getLineColumnRemaining());
    }

    public void writeLineEnd(TerminalLineColumnsWriting writing) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
        writing.nextLine(writing.getLineY() + 1 >= displayItems.size());
    }


    /////////////////////////////////

    public void scrollToNextLineWithCursor() {
        if (cursorLine + 1 >= height) {
            scrollToNextLine();
        } else {
            List<DisplayItem> displayItems = getDisplayItemsWithBuild();
            if (cursorLine < displayItems.size()) {
                cursorLine++;
            }
        }
    }

    public void scrollToPreviousLineWithCursor() {
        if (cursorLine <= 0) {
            scrollToPreviousLine();
        } else {
            --cursorLine;
        }
    }

    public void openOnCursor(boolean open) {
        int idx = getCursorLine();
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
        if (idx >= 0 && idx < displayItems.size()) {
            openDisplayItem(idx, open);
        }
    }
    protected TerminalItem openDisplayItem(int idx, boolean open) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();

        DisplayItem displayItem = displayItems.get(idx);
        TerminalItem item = (open ? tree.open(displayItem.getItem()) : tree.close(displayItem.getItem()));

        List<DisplayItem> old = displayItems;
        displayItems = new ArrayList<>(height);
        this.displayItems = displayItems;
        displayItems.addAll(old.subList(0, idx));

        BuildIndex buildIndex = new BuildIndex(getDisplayMinLine() + idx, idx);
        buildLine(buildIndex, item);

        reuseRestItems(item, old);

        addNextLinesToHeight();
        return item;
    }
    protected void reuseRestItems(TerminalItem item, List<DisplayItem> old) {
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
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
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
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
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();
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
        List<DisplayItem> displayItems = getDisplayItemsWithBuild();

        //an item in displayed items
        int line = getDisplayedItemIndex(item);
        if (line != -1) {
            cursorLine = line;
            return;
        }

        //reconstruct
        //e.g. height=10, cursorLine=6 => [0,1,2,3,4,5,6],7,8,9
        TerminalItem last = item;
        displayItems.clear();
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
        for (DisplayItem item : displayItems) {
            ConsoleLogger.log(i + " : " + item.toString() +
                    (i == cursorLine ? " : cursorLine" : "") +
                    "  " + item.getTokens(tree));
            ++i;
        }
    }
}
