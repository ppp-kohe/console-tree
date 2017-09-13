package csl.console.example;

import org.jline.builtins.Commands;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;

public class LessEx {
    public static void main(String[] args) throws Exception {

        Commands.less(TerminalBuilder.terminal(), System.out, System.err, new File(".").toPath(),
                args);
    }
}
