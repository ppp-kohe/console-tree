package csl.console.view;

import org.jline.keymap.BindingReader;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.EnumSet;

public class ConsoleApplication {
    protected Terminal terminal;
    protected BindingReader reader;
    protected ConsoleMode defaultMode;
    protected ConsoleMode currentMode;

    public ConsoleApplication(ConsoleMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public void initTerminal() {
        try {
            terminal = TerminalBuilder.terminal();
            //terminal.echo(false);
            Attributes attributes = terminal.getAttributes();
            Attributes newAttr = new Attributes(attributes);
            newAttr.setLocalFlags(EnumSet.of(Attributes.LocalFlag.ICANON, Attributes.LocalFlag.ECHO, Attributes.LocalFlag.IEXTEN), false);
            newAttr.setInputFlags(EnumSet.of(Attributes.InputFlag.IXON, Attributes.InputFlag.ICRNL, Attributes.InputFlag.INLCR), false);
            newAttr.setControlChar(Attributes.ControlChar.VMIN, 1);
            newAttr.setControlChar(Attributes.ControlChar.VTIME, 0);
            newAttr.setControlChar(Attributes.ControlChar.VINTR, 0);
            terminal.setAttributes(newAttr);

            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_xmit);

            reader = new BindingReader(terminal.reader());

            defaultMode.init(this);
            setCurrentMode(defaultMode);

            terminal.puts(InfoCmp.Capability.clear_screen);

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
}
