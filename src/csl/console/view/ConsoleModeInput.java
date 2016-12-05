package csl.console.view;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

import java.util.ArrayList;
import java.util.List;

public class ConsoleModeInput extends ConsoleMode {
    protected ConsoleMode backMode;
    protected LineReader reader;

    protected String prompt = "";

    protected EnterCallBack callBack;

    public interface EnterCallBack {
        /** line is null if the process is canceled (Ctrl+C or Ctrl+D) */
        void apply(String line, ConsoleApplication app);
    }

    public ConsoleModeInput() {}

    public ConsoleModeInput(ConsoleApplication app) {
        init(app);
    }

    @Override
    public String getName() {
        return "Input";
    }

    @Override
    public void init(ConsoleApplication app) {
        super.init(app);

        reader = LineReaderBuilder.builder()
                .terminal(app.getTerminal())
                .build();
    }

    public LineReader getReader() {
        return reader;
    }

    public ConsoleMode getBackMode() {
        return backMode;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public String getPrompt() {
        return prompt;
    }
    public void setCallBack(EnterCallBack callBack) {
        this.callBack = callBack;
    }
    public EnterCallBack getCallBack() {
        return callBack;
    }


    public void setCurrentModeAndRunLoop(ConsoleApplication app, ConsoleMode backMode, String prompt, EnterCallBack callBack) {
        this.backMode = backMode;
        setPrompt(prompt);
        setCallBack(callBack);
        app.setCurrentMode(this);
        app.runLoopOnTop();
    }

    @Override
    public void display(ConsoleApplication app) {
        List<AttributedString> lines = getLines(app);
        int h = app.getSize().getRows();
        app.displayFromMode(lines, h - 1, 0);
    }

    @Override
    public List<AttributedString> getLines(ConsoleApplication app) {
        List<AttributedString> lines = new ArrayList<>(backMode.getLines(app));

        //fill  the bottom line with empty
        int h = app.getSize().getRows();
        if (lines.size() >= h) {
            lines.set(h - 1, AttributedString.EMPTY);
        }
        return lines;
    }

    @Override
    public void runRootCommand(ConsoleApplication app) {
        try {
            String line = reader.readLine(prompt == null ? "" : prompt);

            //re-enable arrow keys
            app.getTerminal().puts(InfoCmp.Capability.keypad_xmit);

            end(app);
            callBack.apply(line, app);

        } catch (UserInterruptException|EndOfFileException iex) {
            ConsoleLogger.log("input exit: " + iex);
            end(app);
            callBack.apply(null, app);
        }
    }

    @Override
    public void end(ConsoleApplication app) {
        app.getDisplay().reset();
        app.getDisplay().clear();
        app.setCurrentMode(backMode);
    }
}
