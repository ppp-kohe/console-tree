package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * a ConsoleMode class provides two ways for initialization:
 * <pre>
 *  1) new ConsoleMode().init(app); //separated init call. this can be used for defaultMode
 *  2) new ConsoleMode(app); //immediately calls init within the constructor
 * </pre>
 *
 * <p>
 *
 *
 *  Call flow:
 * <ul>
 *    <li>{@link ConsoleApplication#initTerminalOnTop()}
 *        <ul>
 *            <li>{@link #init(ConsoleApplication)}</li>
 *        </ul>
 *    </li>
 *
 *    <li>{@link ConsoleApplication#exitTerminalOnTop()}</li>
 *
 *    <li>{@link ConsoleApplication#runLoopOnTop()}
 *        <ul>
 *            <li>{@link #runLoopFromApp(ConsoleApplication)}
 *            <ul>
 *                <li>{@link #runLoopBody(ConsoleApplication)}
 *                <ul>
 *                    <li>{@link #display(ConsoleApplication)}
 *                        <ul>
 *                            <li>{@link #getLines(ConsoleApplication)}</li>
 *                            <li>{@link #getCursorRowAndColumn(ConsoleApplication)}</li>
 *                            <li>{@link ConsoleApplication#displayFromMode(List, int, int)}</li>
 *                        </ul>
 *                    </li>
 *                    <li>{@link #runRootCommand(ConsoleApplication)}
 *                        <ul>
 *                            <li>{@link ConsoleCommand#run(ConsoleApplication)}</li>
 *                        </ul>
 *                    </li>
 *                </ul>
 *                </li>
 *            </ul>
 *            </li>
 *        </ul>
 *     </li>
 *
 *     <li> {@link ConsoleApplication#handleOnTop(Terminal.Signal)}
 *         <ul>
 *             <li> {@link #sizeUpdatedFromApp(ConsoleApplication,Size)}</li>
 *         </ul>
 *     </li>
 *
 *     <li> {@link #end(ConsoleApplication)}
 *     <ul>
 *         <li>{@link ConsoleApplication#endFromMode()}</li>
 *     </ul>
 *     </li>
 *
 * </ul>
 */
public class ConsoleMode {
    protected KeyMap<ConsoleCommand> commands;
    protected ConsoleCommand.ConsoleCommandWithName endCommand;

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
        endCommand = ConsoleCommand.command(this::end, "Quit", "")
                .addKeys('q', 'Q', ConsoleCommand.ESC)
                .bind(app, map);
        return map;
    }

    public String getName() {
        return "";
    }

    public KeyMap<ConsoleCommand> getCommands() {
        return commands;
    }

    public ConsoleCommand.ConsoleCommandWithName getEndCommand() {
        return endCommand;
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
        //getLines -> getCursorRowAndColumn
        int[] cursor = getCursorRowAndColumn(app);

        app.displayFromMode(lines, cursor[0], cursor[1]);
    }


    public void runRootCommand(ConsoleApplication app) {
        ConsoleCommand cmd = app.getReader().readBinding(commands);
        cmd.run(app);
    }

    /** the method is dispatched under a signal handler thread instead of main:
     *   you can call {@link #display(ConsoleApplication)}, but you will need to do synchronization.
     *   This can be override the method:
     *   <pre>
     *       public void display(ConsoleApplication app) {
     *           synchronized (this) {
     *               super.display(app);
     *           }
     *       }
     *   </pre>*/
    public void sizeUpdatedFromApp(ConsoleApplication app, Size size) {
    }

    /** called from {@link #display(ConsoleApplication)} */
    public List<AttributedString> getLines(ConsoleApplication app) {
        return Collections.emptyList();
    }

    /** {row, column}. called from {@link #display(ConsoleApplication)}
     *    after {@link #getLines(ConsoleApplication)} */
    public int[] getCursorRowAndColumn(ConsoleApplication app) {
        return new int[] {0, 0};
    }

    public void end(ConsoleApplication app) {
        app.endFromMode();
    }


    public TerminalItem getKeyHelp() {
        return new TerminalItemNode(
            commands.getBoundKeys().values().stream()
                .distinct()
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
}
