package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class ConsoleModeMessage extends ConsoleMode {
    protected ConsoleMode backMode;
    protected List<AttributedString> messageLines = new ArrayList<>();

    public ConsoleModeMessage(ConsoleMode backMode) {
        this.backMode = backMode;
    }

    public ConsoleModeMessage(ConsoleApplication app, ConsoleMode backMode) {
        this(backMode);
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

    public void add(String line) {
        add(new AttributedStringBuilder().append(line).toAttributedString());
    }

    public void add(AttributedString line) {
        messageLines.add(line);
    }

    public void setMessageLines(List<AttributedString> messageLines) {
        this.messageLines = messageLines;
    }

    public List<AttributedString> getMessageLines() {
        return messageLines;
    }

    public void setCurrentModeAndRunLoop(ConsoleApplication app) {
        app.setCurrentMode(this);
        app.runLoopOnTop();
    }

    @Override
    public List<AttributedString> getLines(ConsoleApplication app) {
        List<AttributedString> lines = new ArrayList<>(backMode.getLines(app));
        //wrapping lines
        List<AttributedString> mLines = new ArrayList<>(this.messageLines.size());
        int w = app.getSize().getColumns();
        for (AttributedString line : messageLines) {
            int lineLen = line.columnLength();
            while (lineLen > w) {
                mLines.add(line.columnSubSequence(0, w));
                line = line.columnSubSequence(w, lineLen);
                lineLen = line.columnLength();
            }
            mLines.add(line);
        }
        messageLines.clear();


        int end = lines.size();
        int h = app.getSize().getRows();
        while (lines.size() < h && !mLines.isEmpty()) {
            AttributedString lastLine = mLines.remove(mLines.size() - 1);
            lines.add(end, lastLine);
        }
        end--;
        while (end > 0 && !mLines.isEmpty()) {
            lines.set(end, mLines.remove(mLines.size() - 1));
            end--;
        }
        return lines;
    }


    @Override
    public void end(ConsoleApplication app) {
        app.setCurrentMode(backMode);
    }
}
