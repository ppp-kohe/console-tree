package csl.console.example;

import csl.console.view.*;
import org.jline.utils.AttributedString;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileBrowser {
    public static void main(String[] args) {
        new FileBrowser().run(new File("."));
    }

    ConsoleApplication app;

    public void run(File dir) {
        ConsoleModeTree mode = new ConsoleModeTree(new TerminalTreeBase());
        app = mode.makeApp();
        app.initTerminal();

        mode.run(app, new FileNode(dir));
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
        public List<TerminalItem> getChildren() {
            if (children == null) {
                if (file.isDirectory()) {
                    children = Arrays.stream(file.listFiles())
                            .map(FileNode::new)
                            .collect(Collectors.toList());;
                } else {
                    children = Collections.emptyList();
                }
            }
            return children;
        }
    }
}
