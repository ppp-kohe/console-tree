package csl.console.view;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <ul>
 *     <li> lineColumn... : physical indices in the line buffer </li>
 *     <li> logicalColumn...:  logical indices in the virtual column buffer,
 *           which starts with 0 and may go beyond the line buffer's width</li>
 *     <li> displayColumn...:  sub-ranges in the virtual column buffer with the logical indices,
 *            indicating actually displaying ranges </li>
 *
 * </ul>
 * <pre>
 *
 *         0:
 *         1:
 *         2:
 *        ...
 *     lineY: [ ...    |lineColumnStart   lineX                    lineColumnEnd|] width
 *               |0-----------------------logicalColumnX---------------------logicalColumnWidth| //virtual column buffer
 *               [0    |displayColumnStart----------------------displayColumnEnd|] //in the virtual column buffer
 *                     [                                                         ] displayColumnWidth
 *
 *              //logicalColumnX = lineX - lineColumnStart + displayColumnStart
 *              //displayColumnEnd = lineColumnEnd - lineColumnStart + displayColumnStart
 * </pre>
 *
 * <pre>
 *    example:
 *     [ lineNum:|...the line buffer's texts are here. Sometimes, you cannot display the full wo...]
 *    =&gt; //scrolling to the right
 *     [ lineNum:|...play the full words due to the width. But, you do not want to scroll lineNu...]
 * </pre>
 */
public class TerminalLineColumnsWriting {
    protected List<AttributedString> lines;
    protected AttributedStringBuilder appendable;

    protected int width;

    protected int lineX;
    protected int lineColumnStart;
    protected int lineColumnEnd;

    protected int displayColumnStart;

    protected int lineY;

    protected int lineCursorX;

    public TerminalLineColumnsWriting(int width, int height) {
        this(new ArrayList<>(height), width);
    }
    public TerminalLineColumnsWriting(List<AttributedString> lines, int width) {
        this.lines = lines;
        this.width = width;
        reset(width);
    }

    public void reset(int width) {
        this.width = width;
        lines.clear();
        initAppendable();
        nextColumn(0);
    }

    public List<AttributedString> getLines() {
        return lines;
    }

    protected void initAppendable() {
        appendable = new AttributedStringBuilder(Math.min(width * 3, 2000));
    }

    /**
     * <pre>
     *     lineY: ...  lineX
     *     ...  :
     *     =&gt;
     *     ...  : ...  \n
     *     lineY: lineX=0
     * </pre>
     */
    public void nextLine(boolean newLine) {
        lineY++;
        lineX = 0;

        lines.add(appendable.toAttributedString());
        initAppendable();
        nextColumn(0);
    }

    /**
     * <pre>
     *   case1:  [... lineX |---remainingOfColumnWidth----|    ] width
     *       =&gt; lineX+remainingColumnWidth
     *
     *   case2:  [... lineX |----------]width----remainingOfColumnWidth----|
     *       =&gt; width
     * </pre>
     */
    private int getLineColumnEnd(int remainingOfColumnWidth) {
        int n = Math.min(width, remainingOfColumnWidth + lineX);
        if (n < 0) { //overflow
            return width;
        } else {
            return n;
        }
    }

    public boolean nextColumnWithCursor(int displayColumnStartAsCursor) {
        return nextColumnWithCursor(displayColumnStartAsCursor, Integer.MAX_VALUE);
    }


    /**
     * it automatically determines displayColumnStart by logicalCursorX.
     * <ol>
     *     <li>Normally it sets lineX to the cursor position and
     *          fits the left side (0) of the virtual column to the left side of the physical one.
     *        <pre>
     *           |lineColumnStart----------lineX=lineCursorX---------------...|
     *           [displayColumnStart=0-----logicalColumnX=logicalCursorX---...]
     *        </pre>
     *     </li>
     *     <li>
     *         if the cursor position is over the physical column width,
     *            it set the cursor position to the right side of the physical column.
     *         <pre>
     *                              |lineColumnStart-----lineX=lineCursorX|width
     *   [0-------------------------|displayColumnStart--logicalColumnX   |------]
     *                                                    =logicalCursorX
     *         </pre>
     *     </li>
     * </ol>
     */
    public boolean nextColumnWithCursor(int logicalCursorX, int logicalColumnWidth) {
        logicalCursorX++;
        int lineColumnStart = lineX;
        int lineColumnWidth = Math.min(Math.max(width - lineColumnStart, 0), logicalColumnWidth);
        if (logicalCursorX < lineColumnWidth) {
            lineCursorX = lineColumnStart + logicalCursorX;
            return nextColumn(0, logicalColumnWidth);
        } else {
            lineCursorX = getLineColumnEnd(logicalColumnWidth);
            //    [0--------------| <----------------lineColumnWidth----logicalCursorX|--------] logicalColumnWidth
            //                    displayColumnStart
            return nextColumn(logicalCursorX - lineColumnWidth, logicalColumnWidth);
        }
    }
    public boolean nextColumn(int displayColumnStart) {
        return nextColumn(displayColumnStart, Integer.MAX_VALUE);
    }

    public boolean nextColumn(int displayColumnStart, int logicalColumnWidth) {
        lineColumnStart = lineX;
        lineColumnEnd = getLineColumnEnd(logicalColumnWidth - displayColumnStart);
        this.displayColumnStart = displayColumnStart;
        return lineColumnEnd > lineX;
    }


    ////////////////////////////////////////////

    public void append(AttributedString token, boolean advanceOnly) {
        int logicalTokenRangeStart = getLogicalColumnX();
        int logicalTokenRangeEnd =  logicalTokenRangeStart + token.length();

        if (isInsideOfDisplayedColumn(logicalTokenRangeStart, logicalTokenRangeEnd)) {
            appendEntire(token, advanceOnly);
        } else if (isRightPartOfDisplayedColumn(logicalTokenRangeStart, logicalTokenRangeEnd)) {
            appendEdge(token, true, getDisplayColumnEnd() - logicalTokenRangeStart, advanceOnly);
        } else if (isLeftPartOfDisplayedColumn(logicalTokenRangeStart, logicalTokenRangeEnd)) {
            appendEdge(token, false, logicalTokenRangeEnd - displayColumnStart, advanceOnly);
        } else {
            appendOut(token, advanceOnly);
        }
    }


    public boolean isInsideOfDisplayedColumn(int logicalColumnRangeStart, int logicalColumnRangeEnd) {
        return displayColumnStart <= logicalColumnRangeStart && logicalColumnRangeEnd <= getDisplayColumnEnd();
    }

    public boolean isRightPartOfDisplayedColumn(int logicalColumnRangeStart, int logicalColumnRangeEnd) {
        //  |displayColumnStart------[logicalColumnRangeStart------displayColumnEnd|------logicalColumnRangeEnd]
        int displayColumnEnd = getDisplayColumnEnd();
        return displayColumnStart <= logicalColumnRangeStart &&
                logicalColumnRangeStart < displayColumnEnd &&
                displayColumnEnd < logicalColumnRangeEnd;

    }
    public boolean isLeftPartOfDisplayedColumn(int logicalColumnRangeStart, int logicalColumnRangeEnd) {
        //  [logicalColumnRangeStart------|displayColumnStart------logicalColumnRangeEnd]------displayColumnEnd|
        int displayColumnEnd = getDisplayColumnEnd();
        return logicalColumnRangeStart <= displayColumnStart &&
                displayColumnStart <= logicalColumnRangeEnd &&
                logicalColumnRangeEnd <= displayColumnEnd;
    }


    protected void appendEntire(AttributedString token, boolean advanceOnly) {
        lineX += token.columnLength();
        if (!advanceOnly) {
            appendable.append(token);
        }
    }

    protected void appendEdge(AttributedString token, boolean leftOrRight, int length, boolean advanceOnly) {
        int len = token.columnLength();
        lineX += length;
        if (!advanceOnly) {
            if (leftOrRight) {
                appendable.append(token.columnSubSequence(0, length));
            } else {
                appendable.append(token.columnSubSequence(len - length, length));
            }
        }
    }

    public void appendOut(AttributedString token, boolean advanceOnly) {
        //nothing
    }

    public int getLogicalColumnX() {
        return lineX - lineColumnStart + displayColumnStart;
    }

    public int getDisplayColumnEnd() {
        return lineColumnEnd - lineColumnStart + displayColumnStart;
    }

    public void append(AttributedString token) {
        append(token, false);
    }

    public void advance(AttributedString token) {
        append(token, true);
    }

    public AttributedStringBuilder getAppendable() {
        return appendable;
    }

    ////////////////////////////////////////////

    public AttributedStyle getStyle() {
        return appendable.style();
    }

    public void setStyle(AttributedStyle style) {
        appendable.style(style);
    }

    public int getLineCursorX() {
        return lineCursorX;
    }

    public int getLineY() {
        return lineY;
    }


    ////////////////////////////////////////////

    public void append(String str) {
        append(new AttributedString(str));
    }

    public void appendSpace(int n) {
        char[] ss = new char[n];
        Arrays.fill(new char[n], ' ');
        append(String.valueOf(ss));
    }

    public int getLineColumnRemaining() {
        return Math.min(0, lineColumnEnd - lineX);
    }
}
