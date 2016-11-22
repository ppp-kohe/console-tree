package csl.console.example;

import csl.console.view.ConsoleApplication;
import csl.console.view.ConsoleMode;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

public class Exp {
    public static void main(String[] args) throws Exception {

        System.out.println("HELLO");
        ConsoleApplication app = new ConsoleApplication(new ConsoleMode());

        app.initTerminal();

        try {
            Terminal term = app.getTerminal();
            term.puts(InfoCmp.Capability.over_strike);
            term.writer().append(new AttributedString("hello").toAnsi(term));
            term.puts(InfoCmp.Capability.parm_right_cursor, 1);
            if (term.puts(InfoCmp.Capability.insert_character)) {
                term.writer().append(new AttributedString("<-").toAnsi(term));
            } else if (term.puts(InfoCmp.Capability.parm_ich, 10)) {
                term.writer().append(new AttributedString("<=").toAnsi(term));
            } else {
                term.writer().append(new AttributedString("!!!").toAnsi(term));
            }

            term.writer().append(new AttributedString("world").toAnsi(term));
            term.puts(InfoCmp.Capability.cursor_down);
            term.puts(InfoCmp.Capability.column_address, 20);
            //term.puts(InfoCmp.Capability.carriage_return);
            term.writer().append(new AttributedString("nextLine").toAnsi(term));
            term.flush();

            while (true) {
                Thread.sleep(10000);
            }

        } finally {
            app.exitTerminal();
        }

    }
}
