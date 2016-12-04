package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     ConsoleModeMessage m = new ConsoleModeMessage(app);
 *
 *     m.setMessageLine(lines);
 *     m.setCurrentModeAndRunLoop(app, backMode);
 * </pre>
 */
public class ConsoleModeMessage extends ConsoleMode {
    protected ConsoleMode backMode;
    protected List<AttributedString> messageLines = new ArrayList<>();
    protected int lastMessageTopRow = 0;
    protected int[] lastMessageEnd = new int[2];

    public ConsoleModeMessage() {
    }

    public ConsoleModeMessage(ConsoleApplication app) {
        init(app);
    }

    @Override
    public String getName() {
        return "Message";
    }

    @Override
    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> keys = super.initCommands(app);
        keys.setNomatch(this::end);
        return keys;
    }

    public void clear() {
        messageLines.clear();
    }

    public void add(String line) {
        add(new AttributedStringBuilder().append(line).toAttributedString());
    }

    public void add(AttributedString line) {
        messageLines.add(line);
    }

    public void setMessageLines(List<AttributedString> messageLines) {
        this.messageLines = new ArrayList<>(messageLines);
    }

    public List<AttributedString> getMessageLines() {
        return messageLines;
    }

    public void setCurrentModeAndRunLoop(ConsoleApplication app, ConsoleMode backMode) {
        this.backMode = backMode;
        app.setCurrentMode(this);
        app.runLoopOnTop();
    }

    @Override
    public List<AttributedString> getLines(ConsoleApplication app) {
        List<AttributedString> lines = new ArrayList<>(backMode.getLines(app));
        //wrapping lines
        List<AttributedString> messLines = new ArrayList<>(this.messageLines.size());
        int w = app.getSize().getColumns();
        for (AttributedString line : this.messageLines) {
            int lineLen = line.columnLength();
            while (lineLen > w) {
                messLines.add(line.columnSubSequence(0, w));
                line = line.columnSubSequence(w, lineLen);
                lineLen = line.columnLength();
            }
            messLines.add(line);
        }


        int end = lines.size();
        int h = app.getSize().getRows();
        int messageTop = end;
        while (lines.size() < h && !messLines.isEmpty()) {
            AttributedString lastLine = messLines.remove(messLines.size() - 1);
            lines.add(end, lastLine);
            /* mess:[m1,m2,m3,m4] lines:[l1,l2,l3]
            -> mess:[m1,m2,m3]    lines:[l1,l2,l3,m4]
            -> mess:[m1,m2]       lines:[l1,l2,l3,m3,m4]
             */
        }
        end--;
        while (end > 0 && !messLines.isEmpty()) {
            messageTop = end;
            lines.set(end, messLines.remove(messLines.size() - 1));
            end--;

            /* mess:[m1,m2]       lines:[l1,l2,l3,m3,m4]
            -> mess:[m1]          lines:[l1,l2,m2,m3,m4]
            -> mess:[]            lines:[l1,m1,m2,m3,m4]
             */
        }
        lastMessageTopRow = messageTop;
        lastMessageEnd = new int[] {
            Math.max(0, lines.size() - 1),
            Math.min(lines.isEmpty() ? 0 : lines.get(lines.size() - 1).columnLength() + 1, app.getSize().getColumns())
        };
        return lines;
    }

    @Override
    public int[] getCursorRowAndColumn(ConsoleApplication app) {
        return lastMessageEnd;
    }

    @Override
    public void end(ConsoleApplication app) {
        clear();
        app.setCurrentMode(backMode);
    }
}
