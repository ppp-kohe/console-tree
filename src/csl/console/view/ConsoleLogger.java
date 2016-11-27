package csl.console.view;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ConsoleLogger {
    private static volatile LogDest global;

    /**
     * use the property "csl.console.log", which takes the form of "col[,col]...":
     *   col is one of
     *     <ul>
     *         <li> "file:path" : specifies a file path by the "path" as the destination</li>
     *         <li> "err" : specifies System.err </li>
     *         <li> "true" : specifies Swing GUI console </li>
     *         <li> "false" : no logging </li>
     *     </ul>
     * The default is mere "false".
     */
    public static void log(String msg) {
        if (global == null) {
            setup();
        }
        global.accept(msg);
    }

    public static void closeLog() {
        if (global != null) {
            global.close();
        }
    }

    private static void setup() {
        String type = System.getProperty("csl.console.log", "false");

        LogDest f = null;
        for (String col : type.split(Pattern.quote(","))) {
            if (col.startsWith("file:")) {
                File file = new File(col.substring("file:".length()));
                f = compose(f, new LogDestFile(file));
            } else if (col.equals("err")) {
                f = compose(f, System.err::println);
            } else if (!col.equals("false")) {
                f = compose(f, new LogDestGui());
            }
        }
        if (f == null) {
            f = (line) -> {};
        }
        global = f;
    }


    static LogDest compose(LogDest l, LogDest r) {
        if (l == null) {
            return r;
        } else {
            return new LogDestAnd(l, r);
        }
    }

    public interface LogDest {
        void accept(String line);
        default void close() {}
    }
    public static class LogDestAnd implements LogDest {
        LogDest left;
        LogDest right;

        public LogDestAnd(LogDest left, LogDest right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public void accept(String line) {
            left.accept(line);
            right.accept(line);
        }

        @Override
        public void close() {
            left.close();
            right.close();
        }
    }
    public static class LogDestFile implements LogDest {
        OutputStreamWriter writer;
        public LogDestFile(File file) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void accept(String line) {
            try {
                writer.write(line + "\n");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void close() {
            try {
                writer.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public static class LogDestGui implements LogDest {
        ConsoleLogger logger  = new ConsoleLogger();
        @Override
        public void accept(String line) {
            logger.add(line);
        }

        @Override
        public void close() {
            logger.close();
        }
    }


    JFrame frame;
    JTextArea text;
    JScrollPane scrollPane;

    protected void init() {
        frame = new JFrame("Logger");
        {
            text = new JTextArea();
            text.setFont(new Font("Menlo", Font.PLAIN, 12));
            text.setMargin(new Insets(3, 3, 3, 3));

            scrollPane = new JScrollPane(text);
            scrollPane.setPreferredSize(new Dimension(800, 600));
            frame.getContentPane().add(scrollPane);

        }
        frame.setAutoRequestFocus(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void add(String line) {
        add(line, true);
    }

    public void add(String line, boolean event) {
        if (!event) {
            if (frame == null) {
                init();
            }
            Document doc = text.getDocument();
            try {
                doc.insertString(doc.getLength(), line + "\n", null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            SwingUtilities.invokeLater(() -> add(line, false));
        }
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

}
