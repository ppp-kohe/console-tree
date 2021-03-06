package csl.console.view;

import org.jline.keymap.BindingReader;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.List;

/**
 * You can define your {@link ConsoleMode} class.
 * <pre>
 *     public class MyMode extends ConsoleMode {
 *         &#64;Override
 *         public List&lt;AttributedString&gt; getLines(ConsoleApplication app) {
 *            ...
 *         }
 *         ...
 *     }
 * </pre>
 *
 * <pre>
 *   public static void main(String[] args) {
 *     ConsoleApplication app = new MyMode().makeApp();
 *     app.initTerminalOnTop();
 *     try {
 *         ...//provides specific info. for  defaultMode
 *         app.runLoopOnTop();
 *     } finally {
 *         app.exitTerminalOnTop();
 *     }
 *   }
 * </pre>
 */
public class ConsoleApplication {
    protected Terminal terminal;
    protected BindingReader reader;
    protected ConsoleMode defaultMode;
    protected ConsoleMode currentMode;
    protected Attributes prevAttributes;
    protected Terminal.SignalHandler prevHandler;
    protected volatile Size size;
    protected boolean sizeChanged;

    protected Display display;

    public ConsoleApplication(ConsoleMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public void initTerminalOnTop() {
        try {
            terminal = TerminalBuilder.builder()
                    .nativeSignals(true)
                    .build();
            prevAttributes = terminal.enterRawMode();

            prevHandler = terminal.handle(Terminal.Signal.WINCH, this::handleOnTop);

            reader = new BindingReader(terminal.reader());

            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_xmit);
            terminal.puts(InfoCmp.Capability.clear_screen);

            size = terminal.getSize();

            display = new Display(terminal, true);
            display.clear();

            defaultMode.init(this);
            setCurrentMode(defaultMode);
        } catch (IOException ioe) {
            error(ioe);
        }
    }

    public void exitTerminalOnTop() {
        try {
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_local);
            terminal.flush();
            terminal.setAttributes(prevAttributes);
            terminal.handle(Terminal.Signal.WINCH, prevHandler);
            terminal.close();

            ConsoleLogger.closeLog();
        } catch (IOException ioe) {
            error(ioe);
        }
    }

    public void setCurrentMode(ConsoleMode currentMode) {
        this.currentMode = currentMode;
    }
    public ConsoleMode getCurrentMode() {
        return currentMode;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void error(Exception e) {
        //TODO
        throw new RuntimeException(e);
    }

    ///////////////////////

    public void runLoopOnTop() {
        while (currentMode != null) {
            currentMode.runLoopFromApp(this);
        }
    }

    /** automatically called when WINCH (window size change) */
    public synchronized void handleOnTop(Terminal.Signal signal) {
        if (signal.equals(Terminal.Signal.WINCH)) {
            size = terminal.getSize();
            sizeChanged = true;
            currentMode.sizeUpdatedFromApp(this, size);
        }
    }

    public Size getSize() {
        return size;
    }

    public Display getDisplay() {
        return display;
    }

    public BindingReader getReader() {
        return reader;
    }

    public ConsoleMode getDefaultMode() {
        return defaultMode;
    }


    ///////////////////////

    public void endFromMode() {
        setCurrentMode(null);
    }

    public void displayFromMode(List<AttributedString> lines, int cursorRow, int cursorColumn) {
        if (sizeChanged) {
            sizeChanged = false;
            display.clear();
        }
        int r = Math.max(1, size.getRows());
        int c = Math.max(1, size.getColumns());
        display.resize(r, c);
        display.update(lines, size.cursorPos(cursorRow, cursorColumn));
        terminal.flush();
    }


}
