package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * a ConsoleMode class provides two ways for initialization:
 * <pre>
 *  1) new ConsoleMode().init(app); //separated init call. this can be used for defaultMode
 *  2) new ConsoleMode(app); //immediately calls init within the constructor
 * </pre>
 */
public class ConsoleMode {
    protected KeyMap<ConsoleCommand> commands;

    public ConsoleApplication makeApp() {
        return new ConsoleApplication(this);
    }

    public ConsoleMode(ConsoleApplication app) {
        init(app);
    }

    /** no initialization */
    public ConsoleMode() {
    }

    public void init(ConsoleApplication app) {
        commands = initCommands(app);
    }

    protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
        KeyMap<ConsoleCommand> map = new KeyMap<>();
        ConsoleCommand.command(this::end, "Quit", "")
                .addKeys('q', 'Q', ConsoleCommand.ESC)
                .bind(app, map);
        return map;
    }

    public String getName() {
        return "";
    }

    public void runLoopFromApp(ConsoleApplication app) {
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
        List<AttributedString> lines = new ArrayList<>(getLines(app));
        int[] cursor = getCursorRowAndColumn(app);

        app.displayFromMode(lines, cursor[0], cursor[1]);
    }


    public void runRootCommand(ConsoleApplication app) {
        ConsoleCommand cmd = app.getReader().readBinding(commands);
        cmd.run(app);
    }

    /** the method is dispatched under a signal handler thread instead of main */
    public void sizeUpdatedFromApp(Size size) {
    }

    /** called from {@link #display(ConsoleApplication)} */
    public List<AttributedString> getLines(ConsoleApplication app) {
        return Collections.emptyList();
    }

    /** {row, column}. called from {@link #display(ConsoleApplication)} */
    public int[] getCursorRowAndColumn(ConsoleApplication app) {
        return new int[] {0, 0};
    }

    public void end(ConsoleApplication app) {
        app.endFromMode();
    }


    public TerminalItem getKeyHelp() {
        return new TerminalItemNode(
            commands.getBoundKeys().values().stream()
                .map(command ->
                    new TerminalItemLine().withColumnTokens(
                            TerminalItemLine.toSingleStringColumnsFromStrings(
                                keySeqListName(command.getKeys()), "  :  ",
                                command.getName(), "    ", command.getDescription()))
                )
                .collect(Collectors.toList()))
                .withColumnTokens(TerminalItemLine.toSingleStringColumnsFromStrings(getName()));
    }

    private String keySeqListName(List<List<ConsoleCommand.Key>> keySeqList) {
        return keySeqList.stream()
                .map(this::keySeqName)
                .reduce("", (p,keySeqStr) -> p.isEmpty() ? keySeqStr : p + ", " + keySeqStr);
    }

    private String keySeqName(List<ConsoleCommand.Key> keySeq) {
        String s = keySeq.stream()
                .map(ConsoleCommand.Key::getName)
                .reduce("", (p,keyStr) -> p.isEmpty() ? keyStr : p + " " + keyStr);
        return s;
    }

    public String toKeyName(String keyCode) {
        StringBuilder buf = new StringBuilder();
        for (char c : KeyMap.translate(keyCode).toCharArray()) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(Character.getName(0xFFFF & c));
        }
        return buf.toString();
    }
}
