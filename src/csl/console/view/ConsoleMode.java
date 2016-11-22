package csl.console.view;

import org.jline.keymap.KeyMap;

public class ConsoleMode {
    protected KeyMap<ConsoleCommand> commands;

    public void init(ConsoleApplication app) {
        commands = initCommands(app);
    }

    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        return new KeyMap<>();
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
        displayWithoutFlush(app);
        app.getTerminal().flush();
    }

    /** a subclass needs to override the method */
    public void displayWithoutFlush(ConsoleApplication app) {
    }


    public void runRootCommand(ConsoleApplication app) {
        //TODO
    }
}
