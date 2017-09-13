package csl.console.test;

import csl.console.view.*;
import org.jline.keymap.KeyMap;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass=csl.console.test.Test05TerminalTreeViewExp
public class Test05TerminalTreeViewExp {
    public static void main(String[] args) {
        System.setProperty("csl.console.log", "true");
        ConsoleApplication app = new TestMode().makeApp();
        try {
            app.initTerminalOnTop();
            app.runLoopOnTop();
        } finally {
            app.exitTerminalOnTop();
        }
    }

    public static class TestMode extends ConsoleMode {
        TerminalTreeView view;
        List<TerminalItem> items;
        @Override
        protected KeyMap<ConsoleCommand> initCommands(ConsoleApplication app) {
            KeyMap<ConsoleCommand> km = super.initCommands(app);

            ConsoleCommand.command(this::up, "up", "Up")
                .addKey(InfoCmp.Capability.key_up)
                .bind(app, km);
            ConsoleCommand.command(this::down, "down", "Down")
                .addKey(InfoCmp.Capability.key_down)
                .bind(app, km);

            ConsoleCommand.command(this::logDisplayItems, "log-display-items", "log displayItems")
                .addKeys('l')
                .bind(app, km);

            TerminalItemNode root = new TerminalItemNode().withColumnTokens(TerminalItemLine.toSingleStringColumnsFromStrings("root"));
            items = new ArrayList<>();
            build(root, 0, items);

            TerminalTree tree = new TerminalTreeBase();

            view = new TerminalTreeView(items.get(items.size() / 2), tree);
            view.setWidth(app.getSize().getColumns());
            view.setHeight(app.getSize().getRows());

            items.forEach(tree::open);
            ConsoleLogger.log("items: " + items.size());
            return km;
        }

        @Override
        public List<AttributedString> getLines(ConsoleApplication app) {
            return view.getDisplayItemsWithBuild(false)
                .stream()
                .map(i -> "[" + items.indexOf(i.getItem()) + "] " + i)
                .map(AttributedString::new)
                .collect(Collectors.toList());
        }

        public void up(ConsoleApplication app) {
            TerminalItem current = view.getItemOnCursor();
            TerminalItem prev = view.getTree().getPrevious(current);
            if (prev != null) {
                ConsoleLogger.log("up: " + items.indexOf(prev) + " " + prev);
                view.setOrigin(prev);
                //app.getDisplay().reset();
            }
        }

        public void down(ConsoleApplication app) {
            TerminalItem current = view.getItemOnCursor();
            TerminalItem next = view.getTree().getNext(current);
            if (next != null) {
                ConsoleLogger.log("down: " + items.indexOf(next) + " " + next);
                view.setOrigin(next);
            }
        }

        public void logDisplayItems(ConsoleApplication app) {
            ConsoleLogger.log("------------------------");
            view.getDisplayItems()
                    .forEach(i -> ConsoleLogger.log("[" + items.indexOf(i.getItem()) + "] " + i));
        }
    }


    /*
        root
            line-0 item-0
            line-1 dep-0-node-0
                line-0 dep-1-node-0
                line-1 item-0
                    ...
                ..
            line-17 dep0-node-9
                ...
                      ...
                            line-17 dep-5-node-9
                            line-18 item-9
            line-19 item-9
     */
    public static int build(TerminalItemNode parent, int dep, List<TerminalItem> items) {
        int n = 0;
        int line = 0;
        for (int i = 0; i < 10; ++i) {
            TerminalItemNode node = new TerminalItemNode()
                    .withColumnTokens(TerminalItemLine.toSingleStringColumnsFromStrings("line-" + line, "dep-" + dep + "-node-" + i));
            parent.addChild(node);
            items.add(node);
            ++n;
            if (dep < 5) {
                n += build(node, dep + 1, items);
            }
            ++line;

            TerminalItemLine l = new TerminalItemLine(
                    TerminalItemLine.toSingleStringColumnsFromStrings("line-" + line, "item-" + i));
            parent.addChild(l);
            items.add(l);
            ++n;
            ++line;
        }
        return n;
    }
}
