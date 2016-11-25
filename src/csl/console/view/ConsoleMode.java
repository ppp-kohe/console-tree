package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;

public class ConsoleMode {
    protected KeyMap<ConsoleCommand> commands;

    public void init(ConsoleApplication app) {
        commands = initCommands(app);
    }

    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> map = new KeyMap<>();
        map.bind(ConsoleApplication::end, "q");
        return map;
    }

    public void runLoop(ConsoleApplication app) {
        try {
            while (app.getCurrentMode() == this) {
                runLoopBody(app);
            }
        } catch (Exception ex) {
            app.error(ex);
        }
    }

    public void runLoopBody(ConsoleApplication app) {
        display(app);
        runRootCommand(app);
    }

    public void display(ConsoleApplication app) {
        app.getTerminal().flush();
    }


    public void runRootCommand(ConsoleApplication app) {
        ConsoleCommand cmd = app.getReader().readBinding(commands);
        cmd.run(app);
    }

    public void sizeUpdated(Size size) {

    }
}
