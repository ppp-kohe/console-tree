package csl.console.view;

import org.jline.utils.AttributedString;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TerminalTreeBase implements TerminalTree {
    protected Set<TerminalItem> openItems = new HashSet<>();

    @Override
    public List<AttributedString> getTokens(TerminalItem item) {
        if (item == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new AttributedString(item.toString()));
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
        TerminalItem child = getFirstChild(item);
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
        TerminalItem c = getLastChild(item);
        if (c == null) {
            return item;
        } else {
            return getLast(c);
        }
    }
}
