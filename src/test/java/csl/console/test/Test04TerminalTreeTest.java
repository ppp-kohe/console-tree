package csl.console.test;

import csl.console.view.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class Test04TerminalTreeTest {
    @Test
    public void test() {
        TerminalItemNode root = new TerminalItemNode().withColumnTokens(TerminalItemLine.toSingleStringColumnsFromStrings("root"));
        Set<TerminalItem> items = new HashSet<>();
        int nodes = build(root, 0, items);

        TerminalTreeBase tree = new TerminalTreeBase();
        open(root, tree);
        travCheck(Collections.emptyList(), root, tree, 0);

        travForward(root, tree, nodes, items);
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
    public int build(TerminalItemNode parent, int dep, Set<TerminalItem> items) {
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

    private void open(TerminalItem item, TerminalTree tree) {
        List<TerminalItem> cs = tree.getChildren(item);
        if (cs != null) {
            tree.open(item);
            cs.forEach(i -> open(i, tree));
        }
    }

    private TerminalItem getLast(TerminalItem item) {
        if (item instanceof TerminalItemNode) {
            List<TerminalItem> cs = ((TerminalItemNode) item).getChildren();
            if (cs != null) {
                return getLast(cs.get(cs.size() - 1));
            } else {
                return item;
            }
        } else {
            return item;
        }
    }

    private TerminalItem getPrev(List<TerminalItem> path, TerminalItem item) {
        path = new ArrayList<>(path);
        for (int i = path.size() - 1; i >= 0; --i) {
            TerminalItem parent = path.get(i);
            List<TerminalItem> cs = ((TerminalItemNode) parent).getChildren();
            int idx = cs.indexOf(item);
            if (idx < cs.size() - 1) {
                return cs.get(idx + 1);
            }
            item = parent;
        }
        return null;
    }

    private void travCheck(List<TerminalItem> path, TerminalItem item, TerminalTree tree, int dep) {
        List<TerminalItem> cs = tree.getChildren(item);
        if (item instanceof TerminalItemNode) {
            if (dep > 5) {
                Assert.assertNull("null children", cs);
            } else {
                Assert.assertEquals("20 children", 20, cs.size());
            }
            if (cs != null) {
                for (int i = 0; i < cs.size(); ++i) {
                    TerminalItem it = cs.get(i);

                    String str = "dep-" + dep + " child-" + i;

                    TerminalItem prev = tree.getPrevious(it);
                    if (i == 0) {
                        Assert.assertEquals(str + " prev is parent", item, prev);
                    } else {
                        Assert.assertEquals(str + " prev", getLast(cs.get(i - 1)), prev);
                    }

                    TerminalItem next = tree.getNext(it);
                    if (it instanceof TerminalItemNode && dep + 1 <= 5) {
                        Assert.assertEquals(str + " next is first child", ((TerminalItemNode) it).getChildren().get(0), next);
                    } else if (i == cs.size() - 1) {
                        List<TerminalItem> nextPath = new ArrayList<>(path);
                        nextPath.add(item);
                        Assert.assertEquals(str + " next is parent next sibling", getPrev(nextPath, it), next);
                    } else {
                        Assert.assertEquals(str + " next is a sibling", cs.get(i + 1), next);
                    }
                }
                for (TerminalItem c : cs) {
                    List<TerminalItem> nextPath = new ArrayList<>(path);
                    nextPath.add(item);
                    travCheck(nextPath, c, tree, dep + 1);
                }
            }
        }
    }

    private void travForward(TerminalItem node, TerminalTree tree, int n, Set<TerminalItem> items) {
        TerminalItem next = node;
        TerminalItem last = node;
        int count = 0;
        Set<TerminalItem> nextRemain = new HashSet<>(items);
        while (next != null) {
            last = next;
            nextRemain.remove(next);
            next = tree.getNext(next);
            ++count;
        }

        Assert.assertEquals("forward", n + 1, count);
        Assert.assertEquals("forward all", 0, nextRemain.size());
        System.out.println("last: " + last);

        int backCount = 0;
        TerminalItem top = last;
        Set<TerminalItem> prevRemain = new HashSet<>(items);
        while (last != null) {
            top = last;
            prevRemain.remove(last);
            last = tree.getPrevious(last);
            ++backCount;
        }
        Assert.assertEquals("backward", n + 1, backCount);
        Assert.assertEquals("backward all", 0, prevRemain.size());
        System.out.println("top: " + top);
    }
}
