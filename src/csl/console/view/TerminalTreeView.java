package csl.console.view;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;

public class TerminalTreeView {
    protected TerminalItem origin;
    protected TerminalTree tree;

    protected List<DisplayItem> displayItems;
    protected int displayMaxWidth;

    protected int width = 100;
    protected int height = 30;

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
        this.height = height;
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
    public int getCursorLine() {
        return cursorLine;
    }
    public void setCursorLine(int cursorLine) {
        this.cursorLine = cursorLine;
    }

    /////////////////////////////////

    public void build() {
        displayItems.clear();
        buildLine(new BuildIndex(0, 0), origin);
        addNextLinesToHeight();
        updateOrigin();
        updateDisplayTokens();
    }

    public boolean buildLine(BuildIndex idx, TerminalItem item) {
        if (idx.y < getDisplayMaxLine()) {
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
        TerminalItem child = tree.getFirstChild(item);
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
        return getLineHeadWidth() + item.getMaxWidth();
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

        public DisplayItem(TerminalItem item) {
            this.item = item;
        }

        public TerminalItem getItem() {
            return item;
        }

        public int getMaxWidth() {
            return 0; //TODO
        }
    }

    /////////////////////////////////

    public int getDisplayMaxLine() {
        return offsetY + height;
    }
    public int getDisplayMinLine() {
        return offsetY;
    }


    /////////////////////////////////

    public boolean scrollToNextLine() {
        boolean result = false;
        if (displayItems.size() >= getDisplayMaxLine()) {
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

    public boolean scrollToPreviousLine() {
        DisplayItem removed = null;
        boolean result = false;
        if (displayItems.size() >= getDisplayMaxLine()) {
            removed = displayItems.remove(displayItems.size() - 1);
        }

        if (!displayItems.isEmpty()) {
            DisplayItem last = displayItems.get(0);
            TerminalItem prev = tree.getPrevious(last.getItem());
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


    /*
    public void write(Terminal terminal) {
        terminal.writer().write(write().getAppendable().toAnsi(terminal));
    }*/

    public TerminalLineColumnsWriting write(Terminal terminal) {
        TerminalLineColumnsWriting writing = makeWriting(terminal);
        displayItems.forEach(item ->
                writeLine(writing, writing.getLineY() == cursorLine, item));
        return writing;
    }

    protected TerminalLineColumnsWriting makeWriting(Terminal terminal) {
        return new TerminalLineColumnsWriting(terminal, width);
    }

    public void writeLine(TerminalLineColumnsWriting writing, boolean cursorLine, DisplayItem item) {
        writeLine(writing, cursorLine, tree.getTokens(item.getItem()));
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
        writing.nextLine(writing.getLineY() + 1 >= displayItems.size());
    }


    /////////////////////////////////

    public void scrollToNextLineWithCursor() {
        if (cursorLine + 1 >= height) {
            scrollToNextLine();
        } else {
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
        if (idx >= 0 && idx < displayItems.size()) {
            openDisplayItem(idx, open);
        }
    }
    protected TerminalItem openDisplayItem(int idx, boolean open) {
        DisplayItem displayItem = displayItems.get(idx);
        TerminalItem item = (open ? tree.open(displayItem.getItem()) : tree.close(displayItem.getItem()));

        List<DisplayItem> old = displayItems;
        displayItems = new ArrayList<>(height);
        displayItems.addAll(old.subList(0, idx));

        BuildIndex buildIndex = new BuildIndex(getDisplayMinLine() + idx, idx);
        buildLine(buildIndex, item);

        reuseRestItems(item, old);

        addNextLinesToHeight();
        return item;
    }
    protected void reuseRestItems(TerminalItem item, List<DisplayItem> old) {
        TerminalItem next = tree.getUpperNext(item);
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
}
