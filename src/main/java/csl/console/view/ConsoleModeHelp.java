package csl.console.view;

public class ConsoleModeHelp extends ConsoleModeTree {
    protected ConsoleMode backMode;

    public ConsoleModeHelp() {
        super(new HelpTree());
    }

    public ConsoleModeHelp(ConsoleApplication app) {
        this();
        init(app);
    }

    @Override
    protected void initHelp(ConsoleApplication app) {
        //avoid infinite recursion
    }

    @Override
    public String getName() {
        return "Help";
    }

    public ConsoleMode getBackMode() {
        return backMode;
    }

    public void setCurrentModeAndRunLoop(ConsoleApplication app, ConsoleMode backMode) {
        this.backMode = backMode;
        setCurrentModeAndRunLoop(app, tree.open(backMode.getKeyHelp()));
    }

    @Override
    public void end(ConsoleApplication app) {
        app.setCurrentMode(backMode);
    }

    @Override
    public void showHelp(ConsoleApplication app) {
        end(app);
    }

    public static class HelpTree extends TerminalTreeBase {
        @Override
        public boolean isOpen(TerminalItem item) {
            return true;
        }
    }
}
