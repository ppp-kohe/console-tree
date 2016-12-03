package csl.console.view;

import org.jline.keymap.KeyMap;
import org.jline.utils.InfoCmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface ConsoleCommand {
    void run(ConsoleApplication app);

    default String getName() {
        return "";
    }

    default String getDescription() {
        return "";
    }

    default List<List<Key>> getKeys() {
        return Collections.emptyList();
    }

    static ConsoleCommandWithName command(ConsoleCommand c, String name, String desc) {
        return new ConsoleCommandWithName(c, name, desc);
    }

    class ConsoleCommandWithName implements ConsoleCommand {
        protected ConsoleCommand command;
        protected String name;
        protected String description;

        protected List<List<Key>> keys = Collections.emptyList();

        public ConsoleCommandWithName(ConsoleCommand com, String name, String description) {
            this.command = com;
            this.name = name;
            this.description = description;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public List<List<Key>> getKeys() {
            return keys;
        }

        @Override
        public void run(ConsoleApplication app) {
            command.run(app);
        }

        public ConsoleCommandWithName addKeySequence(Key... keySequence) {
            return addKeySequence(Arrays.asList(keySequence));
        }
        public ConsoleCommandWithName addKeySequence(List<Key> keySequence) {
            if (keys.isEmpty()) {
                keys = new ArrayList<>(3);
            }
            keys.add(keySequence);
            return this;
        }
        public ConsoleCommandWithName addKeys(char... keys) {
            for (char c : keys) {
                addKeySequence(keyLetter(c));
            }
            return this;
        }
        public ConsoleCommandWithName addAltKey(char c) {
            return addKeySequence(alt(c));
        }
        public ConsoleCommandWithName addCtrlKey(char c) {
            return addKeySequence(alt(c));
        }
        public ConsoleCommandWithName addKey(InfoCmp.Capability key) {
            return addKeySequence(key(key));
        }

        public ConsoleCommandWithName bind(ConsoleApplication app, KeyMap<ConsoleCommand> keyMap) {
            keyMap.bind(this, keys.stream()
                    .map(ks -> ks.stream()
                            .map(k -> k.toKey(app))
                            .reduce("", (p,k) -> p + k))
                    .collect(Collectors.toList()));
            return this;
        }

        @Override
        public String toString() {
            return getName() + " : " + getDescription() + " : " + getKeys();
        }
    }

    static char ESC = '\u001b';
    static char DEL = '\u007f';

    static LetterKey keyLetter(char c) {
        return new LetterKey(c);
    }
    static ControlKey ctrl(char c) {
        return new ControlKey(ControlKeyType.Alt, c);
    }
    static ControlKey alt(char c) {
        return new ControlKey(ControlKeyType.Ctrl, c);
    }
    static CapabilityKey key(InfoCmp.Capability key) {
        return new CapabilityKey(key);
    }


    abstract class Key {
        public abstract String getName();
        public abstract String toKey(ConsoleApplication app);

        @Override
        public String toString() {
            return getName();
        }
    }

    class LetterKey extends Key {
        protected char letter;
        public LetterKey(char letter) {
            this.letter = letter;
        }
        public char getLetter() {
            return letter;
        }
        @Override
        public String getName() {
            if (Character.isLetterOrDigit(letter)) {
                return Character.toString(letter);
            } else {
                return "<" + Character.getName(letter) + ">";
            }
        }
        @Override
        public String toKey(ConsoleApplication app) {
            return Character.toString(letter);
        }
    }

    enum ControlKeyType {
        Alt, Ctrl
    }

    class ControlKey extends LetterKey {
        protected ControlKeyType type;
        public ControlKey(ControlKeyType type, char letter) {
            super(letter);
            this.type = type;
        }
        public ControlKeyType getType() {
            return type;
        }
        @Override
        public String getName() {
            if (type.equals(ControlKeyType.Alt)) {
                return "ESC-" + super.getName();
            } else if (type.equals(ControlKeyType.Ctrl)){
                return "^" + super.getName();
            } else {
                return type + "-" + super.getName();
            }
        }
        @Override
        public String toKey(ConsoleApplication app) {
            if (type.equals(ControlKeyType.Alt)) {
                return KeyMap.alt(letter);
            } else if (type.equals(ControlKeyType.Ctrl)) {
                return KeyMap.ctrl(letter);
            } else {
                return Character.toString(letter); //error
            }
        }
    }

    class CapabilityKey extends Key {
        protected InfoCmp.Capability key;
        public CapabilityKey(InfoCmp.Capability key) {
            this.key = key;
        }
        public InfoCmp.Capability getKey() {
            return key;
        }
        @Override
        public String getName() {
            return key.getNames()[0];
        }
        @Override
        public String toKey(ConsoleApplication app) {
            return KeyMap.key(app.getTerminal(), key);
        }
    }


}
