package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;

import java.util.List;

public class ConsoleModeTree extends ConsoleMode {
    protected TerminalTreeView treeView;
    protected TerminalTree tree;


    public ConsoleModeTree(TerminalTree tree) {
        this.tree = tree;
    }

    public ConsoleApplication run(TerminalItem origin) {
        return run(makeApp(), origin);
    }

    public ConsoleApplication run(ConsoleApplication app, TerminalItem origin) {
        app.initTerminal();
        setOrigin(origin);
        try {
            app.runLoop();
        } finally {
            app.exitTerminal();
        }
        return app;
    }

    /** it needs to be called after app.initTerminal() */
    public void setOrigin(TerminalItem origin) {
        treeView.setOrigin(origin);
        treeView.build();
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
    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> keys = super.initCommands(app);

        keys.bind(a -> treeView.openOrCloseOnCursor(),
                "\r", " ");
        keys.bind(a -> treeView.scrollToNextLineWithCursor(),
                "e", "j", KeyMap.ctrl('E'), app.getKeyDown());
        keys.bind(a -> treeView.scrollToPreviousLineWithCursor(),
                "y", "k", KeyMap.ctrl('Y'), KeyMap.ctrl('K'), app.getKeyUp());

        keys.bind(a -> treeView.scrollToNextColumn(),
                app.getKeyRight());
        keys.bind(a -> treeView.scrollToPreviousColumn(),
                app.getKeyLeft());

        keys.bind(a -> treeView.scrollUpPage(),
                "u", KeyMap.ctrl('U'));
        keys.bind(a -> treeView.scrollDownPage(),
                "d", KeyMap.ctrl('D'));

        keys.bind(a -> treeView.moveToParent(), "p");
        keys.bind(a -> treeView.moveToLastChild(), "l");
        keys.bind(a -> treeView.moveToFirstChild(), "f");
        keys.bind(a -> treeView.moveToPreviousSibling(), "r");
        keys.bind(a -> treeView.moveToNextSibling(), "x");

        //TODO search: forward /, backward ?, next n, prev N
        //TODO help h

        return keys;
    }

    @Override
    public void sizeUpdated(Size size) {
        treeView.setWidth(size.getColumns());
        treeView.setHeight(size.getRows());
    }

    @Override
    public int[] getCursorRowAndColumn() {
        return new int[] {treeView.getCursorLine(), 0};
    }

    @Override
    public List<AttributedString> getLines() {
        return treeView.write().getLines();
    }


}
