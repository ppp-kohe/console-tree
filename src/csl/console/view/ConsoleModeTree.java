package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

import java.util.List;

public class ConsoleModeTree extends ConsoleMode {
    protected TerminalTreeView treeView;
    protected TerminalTree tree;
    protected String name = "";

    protected ConsoleModeHelp help;

    public ConsoleModeTree(TerminalTree tree) {
        this.tree = tree;
    }

    public ConsoleModeTree(ConsoleApplication app, TerminalTree tree) {
        this(tree);
        init(app);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** main loop with initializing terminal */
    public static ConsoleApplication start(TerminalTree tree, String name, TerminalItem origin) {
        origin = tree.open(origin);
        ConsoleModeTree consoleTree = new ConsoleModeTree(tree);
        consoleTree.setName(name);
        return consoleTree.start(origin);
    }

    /** main loop with initializing terminal */
    public ConsoleApplication start(TerminalItem origin) {
        return start(makeApp(), origin);
    }

    /** main loop with initializing terminal */
    public ConsoleApplication start(ConsoleApplication app, TerminalItem origin) {
        app.initTerminalOnTop();
        try {
            setCurrentModeAndRunLoop(app, origin);
        } finally {
            app.exitTerminalOnTop();
        }
        return app;
    }

    @Override
    public void init(ConsoleApplication app) {
        super.init(app);
        treeView = new TerminalTreeView(null, tree);
        sizeUpdatedFromApp(app.getSize());
        //TODO search

        initHelp(app);
    }

    protected void initHelp(ConsoleApplication app) {
        this.help = new ConsoleModeHelp(app);
    }

    public void setCurrentModeAndRunLoop(ConsoleApplication app, TerminalItem origin) {
        app.setCurrentMode(this);
        setOrigin(origin);
        app.runLoopOnTop();
    }

    /** it needs to be called after app.initTerminalOnTop() */
    public void setOrigin(TerminalItem origin) {
        treeView.setOrigin(origin);
        treeView.build();
    }

    @Override
    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> keys = super.initCommands(app);

        ConsoleCommand.command(a -> treeView.openOrCloseOnCursor(),
                "Open/Close", "").addKeys('\r', ' ')
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.scrollToNextLineWithCursor(),
                "Next line", "")
                .addKeys('e', 'j').addCtrlKey('E').addKey(InfoCmp.Capability.key_down)
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.scrollToPreviousLineWithCursor(),
                "Previous line", "")
                .addKeys('y', 'k').addCtrlKey('Y').addCtrlKey('K').addKey(InfoCmp.Capability.key_up)
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.scrollToNextColumn(),
                "Right", "")
                .addKey(InfoCmp.Capability.key_right)
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.scrollToPreviousColumn(),
                "Left", "")
                .addKey(InfoCmp.Capability.key_left)
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.scrollUpPage(),
                "Page up", "")
                .addKeys('u').addCtrlKey('U')
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.scrollDownPage(),
                "Page down", "")
                .addKeys('d').addCtrlKey('D')
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.moveToParent(),
                "Move to parent", "")
                .addKeys('p')
                .bind(app, keys);
        ConsoleCommand.command(a -> treeView.moveToLastChild(),
                "Move to last child", "")
                .addKeys('l')
                .bind(app, keys);
        ConsoleCommand.command(a -> treeView.moveToFirstChild(),
                "Move to first child", "")
                .addKeys('f')
                .bind(app, keys);
        ConsoleCommand.command(a -> treeView.moveToPreviousSibling(),
                "Move to previous sibling", "")
                .addKeys('r')
                .bind(app, keys);
        ConsoleCommand.command(a -> treeView.moveToNextSibling(),
                "Move to next sibling", "")
                .addKeys('x')
                .bind(app, keys);

        ConsoleCommand.command(a -> treeView.debugLog(),
                "Debug log", "")
                .addKeys('\\')
                .bind(app, keys);

        //TODO search: forward /, backward ?, next n, prev N

        ConsoleCommand.command(this::showHelp,
                "Help", "")
                .addKeys('h', 'H')
                .bind(app, keys);

        return keys;
    }

    @Override
    public void sizeUpdatedFromApp(Size size) {
        treeView.setWidth(size.getColumns());
        treeView.setHeight(size.getRows());
    }

    @Override
    public int[] getCursorRowAndColumn(ConsoleApplication app) {
        return new int[] {treeView.getCursorLine(), 0};
    }

    @Override
    public List<AttributedString> getLines(ConsoleApplication app) {
        return treeView.write().getLines();
    }

    public void showHelp(ConsoleApplication app) {
        help.setCurrentModeAndRunLoop(app, this);
    }
}
