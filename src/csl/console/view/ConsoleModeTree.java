package csl.console.view;

import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public class ConsoleModeTree extends ConsoleMode {
    protected TerminalTreeView treeView;
    protected TerminalTree tree;

    public ConsoleModeTree(TerminalTree tree) {
        this.tree = tree;
    }

    public ConsoleApplication makeApp() {
        return new ConsoleApplication(this);
    }

    @Override
    public void init(ConsoleApplication app) {
        super.init(app);
        treeView = new TerminalTreeView(null, tree);
        treeView.setWidth(app.getTerminal().getWidth());
        treeView.setWidth(app.getTerminal().getHeight());
        //TODO search

        //TODO help
    }

    public void run(ConsoleApplication app, TerminalItem origin) {
        treeView.setOrigin(origin);
        treeView.build();
        app.runLoop();
    }

    @Override
    public void displayWithoutFlush(ConsoleApplication app) {
        Terminal terminal = app.getTerminal();

        //terminal.puts(InfoCmp.Capability.clear_screen);
        treeView.write(terminal);
    }
}
