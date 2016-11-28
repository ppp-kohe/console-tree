package csl.console.view;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.*;

/**
 * A tree model with supporting {@link TerminalItemLine} and {@link TerminalItemNode}.
 * <p>
 *  There are following options for achieving tree walking
 *  <ol>
 *     <li>overrides {@link TerminalItemNode#getChildren()}</li>
 *
 *     <li>overrides {@link #getParent(TerminalItem)} and {@link #getChildren(TerminalItem)}.
 *             This can support item classes other than {@link TerminalItemLine} and {@link TerminalItemNode}. </li>
 *
 *     <li>overrides {@link #getParent(TerminalItem)},
 *              {@link #getFirstChild(TerminalItem)}, {@link #getLastChild(TerminalItem)},
 *              {@link #getNextSibling(TerminalItem)} and {@link #getPrevious(TerminalItem)}.
 *              This can avoid supplying a fixed List object as children,
 *                 which needs to eagerly access children of a node. </li>
 *  </ol>
 */
public class TerminalTreeBase implements TerminalTree {
    protected Set<TerminalItem> openItems = initOpenItems();
    protected boolean indent = true;

    @Override
    public List<List<AttributedString>> getColumnTokens(TerminalItem item) {
        ArrayList<List<AttributedString>> list = new ArrayList<>(3);
        List<AttributedString> hs = getHeadTokens(item);
        List<AttributedString> ms = getTokens(item);
        List<AttributedString> ts = getTailTokens(item);
        if (hs != null) {
            list.add(hs);
        }
        if (ms != null) {
            list.add(ms);
        }
        if (ts != null) {
            list.add(ts);
        }
        return list;
    }

    public List<AttributedString> getHeadTokens(TerminalItem item) {
        return null;
    }
    public List<AttributedString> getTailTokens(TerminalItem item) {
        return null;
    }

    public TerminalTreeBase withIndent(boolean indent) {
        this.indent = indent;
        return this;
    }
    /** default is true */
    public boolean isIndent() {
        return indent;
    }

    @Override
    public List<AttributedString> getTokens(TerminalItem item) {
        List<AttributedString> attrs = getTokensWithoutIndents(item);
        if (indent) {
            List<AttributedString> attrsWithIndent = new ArrayList<>(attrs.size() + 1);
            attrsWithIndent.add(getIndent(item));
            attrsWithIndent.addAll(attrs);
            return attrsWithIndent;
        } else {
            return attrs;
        }
    }

    public List<AttributedString> getTokensWithoutIndents(TerminalItem item) {
        if (item == null) {
            return Collections.emptyList();
        } else if (item instanceof TerminalItemLine) {
            List<AttributedString> list = ((TerminalItemLine) item).getTokens();
            if (list == null) {
                return Collections.emptyList();
            } else {
                return list;
            }
        } else {
            return Collections.singletonList(new AttributedString(item.toString()));
        }
    }

    public AttributedString getIndent(TerminalItem item) {
        AttributedStringBuilder buf = new AttributedStringBuilder();
        int n = getDepth(item);
        AttributedString indentUnit = getIndentUnit();
        for (int i = 0; i < n; ++i) {
            buf.append(indentUnit);
        }
        return buf.toAttributedString();
    }

    public AttributedString getIndentUnit() {
        return new AttributedString(" ");
    }

    public int getDepth(TerminalItem item) {
        if (item == null) {
            return 0;
        } else if (item instanceof TerminalItemLine) {
            return ((TerminalItemLine) item).getDepth();
        } else {
            int dep = -1;
            while (item != null) {
                item = getParent(item);
                ++dep;
            }
            return dep;
        }
    }

    //////////////////////////////////////

    protected Set<TerminalItem> initOpenItems() {
        return new HashSet<>();
    }


    @Override
    public TerminalItem open(TerminalItem item) {
        if (item != null) {
            openItems.add(item);
        }
        return item;
    }

    @Override
    public TerminalItem close(TerminalItem item) {
        if (item != null) {
            openItems.remove(item);
        }
        return item;
    }

    @Override
    public boolean isOpen(TerminalItem item) {
        if (item == null) {
            return false;
        }
        return openItems.contains(item);
    }

    //////////////////////////////////////

    @Override
    public TerminalItem getParent(TerminalItem item) {
        if (item instanceof TerminalItemLine) {
            return ((TerminalItemLine) item).getParent();
        }
        return null;
    }

    @Override
    public List<TerminalItem> getChildren(TerminalItem item) {
        if (item instanceof TerminalItemNode) {
            return ((TerminalItemNode) item).getChildren();
        }
        return null;
    }


    @Override
    public TerminalItem getFirstChild(TerminalItem item) {
        return getFirstChildByChildren(item);
    }

    @Override
    public TerminalItem getLastChild(TerminalItem item) {
        return getLastChildByChildren(item);
    }

    @Override
    public TerminalItem getNextSibling(TerminalItem item) {
        return getNextSiblingByParentChildren(item);
    }

    @Override
    public TerminalItem getPreviousSibling(TerminalItem item) {
        return getPreviousSiblingByParentChildren(item);
    }


    public TerminalItem getFirstChildByChildren(TerminalItem item) {
        List<TerminalItem> cs = getChildren(item);
        if (cs == null) {
            return null;
        } else if (!cs.isEmpty()) {
            return cs.get(0);
        } else {
            return null;
        }
    }
    public TerminalItem getLastChildByChildren(TerminalItem item) {
        List<TerminalItem> cs = getChildren(item);
        if (cs == null) {
            return null;
        } else if (!cs.isEmpty()) {
            return cs.get(cs.size() - 1);
        } else {
            return null;
        }
    }
    public TerminalItem getNextSiblingByParentChildren(TerminalItem item) {
        TerminalItem parent = getParent(item);
        List<TerminalItem> cs = (parent == null ? null : getChildren(parent));
        if (cs == null) {
            return null;
        } else {
            int i = cs.indexOf(item);
            if (i + 1 < cs.size()) {
                return cs.get(i + 1);
            } else {
                return null;
            }
        }
    }
    public TerminalItem getPreviousSiblingByParentChildren(TerminalItem item) {
        TerminalItem parent = getParent(item);
        List<TerminalItem> cs = (parent == null ? null : getChildren(parent));
        if (cs == null) {
            return null;
        } else {
            int i = cs.indexOf(item);
            if (i - 1 >= 0) {
                return cs.get(i - 1);
            } else {
                return null;
            }
        }
    }

    @Override
    public TerminalItem getNext(TerminalItem item) {
        TerminalItem child = isOpen(item) ? getFirstChild(item) : null;
        if (child != null) {
            return child;
        } else {
            return getUpperNext(item);
        }
    }
    @Override
    public TerminalItem getUpperNext(TerminalItem item) {
        TerminalItem sibling = getNextSibling(item);
        if (sibling != null) {
            return sibling;
        } else {
            TerminalItem parent = getParent(item);
            if (parent != null) {
                return getUpperNext(parent);
            } else {
                return null;
            }
        }
    }

    @Override
    public TerminalItem getPrevious(TerminalItem item) {
        TerminalItem sibling = getPreviousSibling(item);
        if (sibling != null) {
            return getLast(sibling);
        } else {
            return getParent(item);
        }
    }


    @Override
    public TerminalItem getUpperPrevious(TerminalItem item) {
        TerminalItem sibling = getPreviousSibling(item);
        if (sibling != null) {
            return sibling;
        } else {
            return getParent(item);
        }
    }

    public TerminalItem getLast(TerminalItem item) {
        TerminalItem c = isOpen(item) ? getLastChild(item) : null;
        if (c == null) {
            return item;
        } else {
            return getLast(c);
        }
    }
}
