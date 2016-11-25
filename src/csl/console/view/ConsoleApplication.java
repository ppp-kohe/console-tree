package csl.console.view;

import org.jline.keymap.BindingReader;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.EnumSet;

/**
 * <pre>
 *     app.initTerminal();
 *     try {
 *         app.runLoop();
 *     } finally {
 *         app.exitTerminal();
 *     }
 * </pre>
 */
public class ConsoleApplication {
    protected Terminal terminal;
    protected BindingReader reader;
    protected ConsoleMode defaultMode;
    protected ConsoleMode currentMode;
    protected Attributes prevAttributes;
    protected Size size;

    protected Display display;

    public ConsoleApplication(ConsoleMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public void initTerminal() {
        try {
            terminal = TerminalBuilder.terminal();
            prevAttributes = terminal.enterRawMode();

            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_xmit);
            terminal.handle(Terminal.Signal.WINCH, this::handle);

            reader = new BindingReader(terminal.reader());

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

    public void exitTerminal() {
        try {
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_local);
            terminal.flush();
            terminal.setAttributes(prevAttributes);
            terminal.close();
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

    public void runLoop() {
        while (currentMode != null) {
            currentMode.runLoop(this);
        }
    }

    /** automatically called when WINCH (window size change) */
    public void handle(Terminal.Signal signal) {
        if (signal.equals(Terminal.Signal.WINCH)) {
            size = terminal.getSize();
            currentMode.sizeUpdated(size);
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

    ///////////////////////

    public void end() {
        setCurrentMode(null);
    }

}
