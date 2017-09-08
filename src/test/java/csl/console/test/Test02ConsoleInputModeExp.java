package csl.console.test;

import csl.console.view.ConsoleApplication;
import csl.console.view.ConsoleCommand;
import csl.console.view.ConsoleMode;
import csl.console.view.ConsoleModeInput;
import org.jline.keymap.KeyMap;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.ArrayList;
import java.util.List;

public class Test02ConsoleInputModeExp {
    public static void main(String[] args) {
        ConsoleApplication app = new TestMode().makeApp();
        app.initTerminalOnTop();
        try {
            app.runLoopOnTop();
        } finally {
            app.exitTerminalOnTop();
        }
    }

    public static class TestMode extends ConsoleMode {
        String inputString = "";
        @Override
        public List<AttributedString> getLines(ConsoleApplication app) {
            List<String> lines = new ArrayList<>();
            lines.add("type i to start input-mode");

            int rows = app.getSize().getRows();
            int cols = app.getSize().getColumns();

            List<AttributedString> aLines = new ArrayList<>();
            lines.stream()
                    .map(AttributedString::new)
                    .forEach(aLines::add);

            AttributedStringBuilder input = new AttributedStringBuilder();
            aLines.add(input.style(input.style().foreground(AttributedStyle.CYAN))
                    .append(inputString)
                    .toAttributedString());

            for (int i = aLines.size(); i < rows; i++) {
                AttributedStringBuilder buf = new AttributedStringBuilder();
                AttributedStyle def = buf.style();
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
            ConsoleCommand.command(this::startInput, "start-input", "start input")
                    .addKeys('i')
                    .bind(app, km);
            return km;
        }

        public void startInput(ConsoleApplication app) {
            new ConsoleModeInput(app)
                    .setCurrentModeAndRunLoop(app, this, "INPUT:",
                        this::input);
        }

        public void input(String line, ConsoleApplication app) {
            this.inputString = "<" + line + ">";
        }
    }
}
