package csl.console.view;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.util.ArrayList;

public class ConsoleModeTree extends ConsoleMode {
    protected TerminalTreeView treeView;
    protected TerminalTree tree;

    public ConsoleModeTree(TerminalTree tree) {
        this.tree = tree;
    }

    public ConsoleApplication makeApp() {
        return new ConsoleApplication(this);
    }

    public void run(ConsoleApplication app, TerminalItem origin) {
        app.initTerminal();
        treeView.setOrigin(origin);
        treeView.build();
        try {
            app.runLoop();
        } finally {
            app.exitTerminal();
        }
    }

    @Override
    public void init(ConsoleApplication app) {
        super.init(app);
        treeView = new TerminalTreeView(null, tree);
        sizeUpdated(app.getSize());
        //TODO search

        //TODO help
    }

    @Override
    public void display(ConsoleApplication app) {
        Display d = app.getDisplay();
        d.resize(treeView.getHeight(), treeView.getWidth());
        int pos = app.getSize().cursorPos(treeView.getCursorLine(), 0);
        d.update(treeView.write().getLines(), pos);
        app.getTerminal().flush();
    }

    @Override
    public void sizeUpdated(Size size) {
        treeView.setWidth(size.getColumns());
        treeView.setHeight(size.getRows());
    }
}
