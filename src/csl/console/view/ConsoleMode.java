package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsoleMode {
    protected KeyMap<ConsoleCommand> commands;

    public ConsoleApplication makeApp() {
        return new ConsoleApplication(this);
    }

    public void init(ConsoleApplication app) {
        commands = initCommands(app);
    }

    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> map = new KeyMap<>();
        map.bind(ConsoleApplication::end, "q", "Q", KeyMap.esc());
        return map;
    }

    public void runLoop(ConsoleApplication app) {
        try {
            while (app.getCurrentMode() == this && !checkInterruption()) {
                runLoopBody(app);
            }
        } catch (Exception ex) {
            app.error(ex);
        }
    }

    public boolean checkInterruption() {
        Thread.yield();
        return Thread.interrupted();
    }

    public void runLoopBody(ConsoleApplication app) {
        display(app);
        runRootCommand(app);
    }

    public void display(ConsoleApplication app) {
        List<AttributedString> lines = new ArrayList<>(getLines());
        int[] cursor = getCursorRowAndColumn();

        app.display(lines, cursor[0], cursor[1]);
    }


    public void runRootCommand(ConsoleApplication app) {
        ConsoleCommand cmd = app.getReader().readBinding(commands);
        cmd.run(app);
    }

    /** the method is dispatched under a signal handler thread instead of main */
    public void sizeUpdated(Size size) {
    }

    /** called from {@link #display(ConsoleApplication)} */
    public List<AttributedString> getLines() {
        return Collections.emptyList();
    }

    /** {row, column}. called from {@link #display(ConsoleApplication)} */
    public int[] getCursorRowAndColumn() {
        return new int[] {0, 0};
    }
}
