package csl.console.example;

import csl.console.view.*;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileBrowser {
    public static void main(String[] args) {
        new FileBrowser().run(new File("."));
    }

    public void run(File dir) {
        try {
            TerminalTreeBase base = new TerminalTreeBase();
            FileNode root = new FileNode(dir);
            ConsoleModeTree.start(base, "File Browser", root);
        } catch (Throwable ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.close();
            ConsoleLogger.log("error: " + sw.toString());
        }
    }

    public static class FileNode extends TerminalItemNode {
        protected File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            return file.getPath();
        }

        @Override
        public List<List<AttributedString>> getColumnTokens() {
            if (columnTokens == null) {
                columnTokens = Collections.singletonList(getTokens());
            }
            return columnTokens;
        }

        public List<AttributedString> getTokens() {
            AttributedStringBuilder buf = new AttributedStringBuilder();

            AttributedStyle slashStyle = buf.style().foreground(AttributedStyle.CYAN);
            AttributedStyle dirStyle = buf.style().foreground(AttributedStyle.RED);
            AttributedStyle fileStyle = buf.style().foreground(AttributedStyle.BLACK);


            if (parent == null) {
                File f = file.getParentFile();
                List<File> list = new ArrayList<>();
                while (f != null) {
                    if (!f.getName().equals("")) {
                        list.add(f);
                    }
                    f = f.getParentFile();
                }
                Collections.reverse(list);

                boolean first = true;
                for (File sf : list) {
                    if (first) {
                        first = false;
                    } else {
                        buf.style(slashStyle);
                        buf.append('/');
                    }
                    buf.style(dirStyle);
                    buf.append(sf.getName());
                }
                if (!first) {
                    buf.style(slashStyle);
                    buf.append('/');
                }
            }
            if (file.isFile()) {
                buf.style(fileStyle);
                buf.append(file.getName());
            } else {
                buf.style(dirStyle);
                buf.append(file.getName());
                buf.style(slashStyle);
                buf.append('/');
            }
            return Collections.singletonList(buf.toAttributedString());
        }

        @Override
        public List<TerminalItem> getChildren() {
            if (children == null) {
                File[] ls;
                if (file.isDirectory() && (ls = file.listFiles()) != null) {
                    Arrays.stream(ls)
                            .map(FileNode::new)
                            .forEach(this::addChild);
                } else {
                    children = Collections.emptyList();
                }
            }
            return children;
        }

        @Override
        public List<AttributedString> getInfoLines() {
            ArrayList<String> list = new ArrayList<>();
            list.add("path:");
            try {
                list.add(file.getCanonicalPath() + (file.isDirectory() ? "/" : ""));
            }catch (Exception ex) {
                list.add("error " + ex);
            }
            if (file.isFile()) {
                list.add(" size:");
                list.add(Long.toString(file.length()));
            }

            return toSingleLine(list);
        }
    }
}
