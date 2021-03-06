package csl.console.test;

import csl.console.view.ConsoleApplication;
import csl.console.view.ConsoleCommand;
import csl.console.view.ConsoleMode;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.util.ArrayList;
import java.util.List;

// mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass=csl.console.test.Test01ConsoleModeExp
public class Test01ConsoleModeExp {
    public static void main(String[] args) {
        ConsoleApplication app = new TestMode().makeApp();

        app.initTerminalOnTop();
        try {
            app.runLoopOnTop();
        } finally {
            app.exitTerminalOnTop();
        }
    }

    static class TestMode extends ConsoleMode {
        String state = "init : " + Thread.currentThread();
        int count = 0;

        @Override
        public void runLoopBody(ConsoleApplication app) {
            //executed for each key press
            super.runLoopBody(app);
            ++count;
        }

        @Override
        public List<AttributedString> getLines(ConsoleApplication app) {
            List<String> lines = new ArrayList<>();
            lines.add("key count: " + count);
            lines.add("type Q to exit. type x to change the state");
            lines.add(state);
            int rows = app.getSize().getRows();
            int cols = app.getSize().getColumns();

            List<AttributedString> aLines = new ArrayList<>();
            lines.stream()
                    .map(AttributedString::new)
                    .forEach(aLines::add);

            for (int i = aLines.size(); i < rows; i++) {
                AttributedStringBuilder buf = new AttributedStringBuilder();
                AttributedStyle def = buf.style().background(AttributedStyle.CYAN);
                AttributedStyle red = buf.style().foreground(AttributedStyle.RED);
                for (int j = 0; j < cols; ++j) {
                    if (j == cols -1 || i == rows - 1) {
                        buf.style(red);
                        buf.append("=");
                    } else {
                        buf.style(def);
                        buf.append('-');
                    }
                }
                aLines.add(buf.toAttributedString());
            }

            return aLines;
        }

        @Override
        protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
            KeyMap<ConsoleCommand> km = super.initCommands(app);
            ConsoleCommand.command(this::testCommand, "test", "test command")
                    .addKeys('x')
                    .bind(app, km);

            ConsoleCommand.command(ap -> {this.state = "left";}, "left", "left")
                    .addKey(InfoCmp.Capability.key_left)
                    .bind(app, km);

            ConsoleCommand.command(ap -> {this.state = "down";}, "down", "down")
                    .addKey(InfoCmp.Capability.key_down)
                    .bind(app, km);

            ConsoleCommand.command(ap -> {}, "nothing", "nothing")
                    .addKeys(' ')
                    .bind(app, km);
            return km;
        }

        public void testCommand(ConsoleApplication app) {
            state = "testCommand";
        }

        @Override
        public void sizeUpdatedFromApp(ConsoleApplication app, Size size) {
            state = "size: " + size.getRows() +"x" + size.getColumns() + " : " + Thread.currentThread();
            //it does not cause re-displaying but executed under a different thread
            display(app);
        }

        @Override
        public void display(ConsoleApplication app) {
            synchronized (this) {
                super.display(app);
            }
        }
    }
}
