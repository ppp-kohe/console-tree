package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

import java.util.List;

/**
 *  Using the tree mode as default mode:
 * <pre>
 *     ConsoleModeTree.start(new TerminalTreeBase(), appName, rootItem);
 *      //it starts with a ConsoleApplication.
 * </pre>
 *  or
 *  <pre>
 *      ConsoleModeTree treeMode = new ConsoleModeTree(new TerminalTreeBase());
 *
 *      ConsoleApplication app = new ConsoleApplication(treeMode);
 *      treeMode.start(app, rootItem);
 * </pre>
 *
 *  Using the tree mode from another mode:
 *  <pre>
 *      ConsoleModeTree treeMode = new ConsoleModeTree(app, new TerminalTreeBase());
 *
 *      treeMode.setCurrentModeAndRunLoop(app, rootItem);
 *  </pre>
 *
 */
public class ConsoleModeTree extends ConsoleMode {
    protected TerminalTreeView treeView;
    protected TerminalTree tree;
    protected String name = "";

    protected ConsoleModeHelp help;
    protected ConsoleModeMessage message;
    protected ConsoleModeInput search;

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

        initHelp(app);
        initMessage(app);
        initSearch(app);
    }

    protected void initHelp(ConsoleApplication app) {
        this.help = new ConsoleModeHelp(app);
    }

    protected void initMessage(ConsoleApplication app) {
        this.message = new ConsoleModeMessage(app);
    }

    protected void initSearch(ConsoleApplication app) {
        this.search = new ConsoleModeInput(app);
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

    protected ConsoleCommand.ConsoleCommandWithName openOrCloseCommand;
    protected ConsoleCommand.ConsoleCommandWithName nextLinCommand;
    protected ConsoleCommand.ConsoleCommandWithName prevLineCommand;
    protected ConsoleCommand.ConsoleCommandWithName rightCommand;
    protected ConsoleCommand.ConsoleCommandWithName leftCommand;
    protected ConsoleCommand.ConsoleCommandWithName pageUpCommand;
    protected ConsoleCommand.ConsoleCommandWithName pageDownCommand;
    protected ConsoleCommand.ConsoleCommandWithName parentCommand;
    protected ConsoleCommand.ConsoleCommandWithName firstChildCommand;
    protected ConsoleCommand.ConsoleCommandWithName lastChildCommand;
    protected ConsoleCommand.ConsoleCommandWithName nextSiblingCommand;
    protected ConsoleCommand.ConsoleCommandWithName prevSiblingCommand;
    protected ConsoleCommand.ConsoleCommandWithName debugLogCommand;
    protected ConsoleCommand.ConsoleCommandWithName helpCommand;
    protected ConsoleCommand.ConsoleCommandWithName infoCommand;

    @Override
    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> keys = super.initCommands(app);

        openOrCloseCommand = ConsoleCommand.command(a -> treeView.openOrCloseOnCursor(),
                "Open/Close", "").addKeys('\r', ' ')
                .bind(app, keys);

        nextLinCommand = ConsoleCommand.command(a -> treeView.scrollToNextLineWithCursor(),
                "Next line", "")
                .addKeys('e', 'j').addCtrlKey('E').addCtrlKey('N').addKey(InfoCmp.Capability.key_down)
                .bind(app, keys);

        prevLineCommand = ConsoleCommand.command(a -> treeView.scrollToPreviousLineWithCursor(),
                "Previous line", "")
                .addKeys('y', 'k').addCtrlKey('Y').addCtrlKey('K').addCtrlKey('P').addKey(InfoCmp.Capability.key_up)
                .bind(app, keys);

        rightCommand = ConsoleCommand.command(a -> treeView.scrollToNextColumn(),
                "Right", "").addCtrlKey('F')
                .addKey(InfoCmp.Capability.key_right)
                .bind(app, keys);

        leftCommand = ConsoleCommand.command(a -> treeView.scrollToPreviousColumn(),
                "Left", "").addCtrlKey('B')
                .addKey(InfoCmp.Capability.key_left)
                .bind(app, keys);

        pageUpCommand = ConsoleCommand.command(a -> treeView.scrollUpPage(),
                "Page up", "")
                .addKeys('u').addCtrlKey('U')
                .bind(app, keys);

        pageDownCommand = ConsoleCommand.command(a -> treeView.scrollDownPage(),
                "Page down", "")
                .addKeys('d').addCtrlKey('D')
                .bind(app, keys);

        parentCommand = ConsoleCommand.command(a -> treeView.moveToParent(),
                "Move to parent", "")
                .addKeys('p')
                .bind(app, keys);
        lastChildCommand = ConsoleCommand.command(a -> treeView.moveToLastChild(),
                "Move to last child", "")
                .addKeys('l')
                .bind(app, keys);
        firstChildCommand = ConsoleCommand.command(a -> treeView.moveToFirstChild(),
                "Move to first child", "")
                .addKeys('f')
                .bind(app, keys);
        nextSiblingCommand = ConsoleCommand.command(a -> treeView.moveToNextSibling(),
                "Move to next sibling", "")
                .addKeys('x')
                .bind(app, keys);
        prevSiblingCommand = ConsoleCommand.command(a -> treeView.moveToPreviousSibling(),
                "Move to previous sibling", "")
                .addKeys('r')
                .bind(app, keys);

        //TODO search: forward /, backward ?, next n, prev N


        debugLogCommand = ConsoleCommand.command(a -> treeView.debugLog(),
                "Debug log", "")
                .addKeys('\\')
                .bind(app, keys);

        helpCommand = ConsoleCommand.command(this::showHelp,
                "Help", "")
                .addKeys('h', 'H')
                .bind(app, keys);

        infoCommand = ConsoleCommand.command(this::showInfo,
                "Item information", "")
                .addKeys('i', 'I')
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

    public void showInfo(ConsoleApplication app) {
        TerminalItem item = treeView.getItemOnCursor();
        List<AttributedString> infos = treeView.getTree().getInfoLines(item);
        if (infos == null || infos.isEmpty()) {
            infos = TerminalItemLine.toLines("No info.");
        }
        message.setMessageLines(infos);
        message.setCurrentModeAndRunLoop(app, this);
    }
}
