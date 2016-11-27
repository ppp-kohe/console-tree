package csl.console.example;

import csl.console.view.TerminalTreeView;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.DumbTerminal;
import org.jline.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Exp {
    public static void main(String[] args) throws Exception {
        new Exp().test4();
    }

    Terminal terminal;
    long time;
    volatile int w;
    volatile int h;
    volatile boolean changed;
    volatile Size size;

    List<String> logs = new ArrayList<>();

    Attributes prevAttr;
    Terminal.SignalHandler prevHand;

    private void init(boolean set) throws Exception {
        terminal = TerminalBuilder.builder().nativeSignals(true).build();
        //terminal = TerminalBuilder.terminal();
        //terminal = TerminalBuilder.builder().system(false)
          //      .streams(System.in, System.out).build();

        if (set) {
            prevAttr = terminal.enterRawMode();
            prevHand = terminal.handle(Terminal.Signal.WINCH, this::handle);

            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_xmit);

            terminal.puts(InfoCmp.Capability.clear_screen);
        }

        w = Math.max(terminal.getWidth(), 10);
        h = Math.max(terminal.getHeight(), 10);
        size = terminal.getSize();
        time = System.nanoTime();
    }

    private synchronized void handle(Terminal.Signal s) {
        Size size = terminal.getSize();
        w = size.getColumns();
        h = size.getRows();
        this.size = size;
        changed = true;
        log("size: " + w + "," + h +" in " + Thread.currentThread());
    }

    private void log(String str) {
        //Logger logger = Logger.getLogger("csl.console.example");
        //logger.log(Level.INFO, str);
        logs.add(str);
    }

    private void exit() {
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.flush();
        terminal.setAttributes(prevAttr);
        terminal.handle(Terminal.Signal.WINCH, prevHand);

        long diff = System.nanoTime() - time;
        log("time=" + (diff / 1000_000) + " " + w + "," + h + " : " + terminal.getClass());
        for (String l : logs) {
            System.out.println(l);
        }
    }

    private AttributedString gen(int s) {
        int v = 0xF0F0F0;
        int[] colors = {AttributedStyle.BRIGHT, AttributedStyle.CYAN, AttributedStyle.MAGENTA, AttributedStyle.YELLOW};
        AttributedStringBuilder buf = new AttributedStringBuilder();
        for (int i = 0; buf.length() < w; ++i) {
            v = v * 3 + (i + s);
            String n = Integer.toString(v) + "!";
            if (buf.length() + n.length() >= w) {
                break;
            }
            buf.style(buf.style().foreground(colors[Math.abs(v) % colors.length]));
            buf.append(n);
            //System.err.println("<" + Integer.toString(v) + ">");
        }
        return buf.substring(0, Math.min(w, buf.length())).toAttributedString();
    }

    public void test1()  {
        try {
            init(true);
            log("test1");
            /////////////

            //terminal.writer().append("hello " + w + " " + gen(10) + ":");
            //terminal.puts(InfoCmp.Capability.carriage_return);

            terminal.flush();


            for (int i = 0; i < 10000; ++i) {
                terminal.writer().append(gen(i).toAnsi(terminal));

                terminal.puts(InfoCmp.Capability.carriage_return);
                if (h >= i) {
                    terminal.puts(InfoCmp.Capability.scroll_forward);
                } else {
                    terminal.puts(InfoCmp.Capability.cursor_down);
                }
                terminal.flush();
                Thread.sleep(1);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            exit();
        }

    }


    public void test2() {
        try {
            init(true);
            log("test2");

            Display d = new Display(terminal, true);

            d.resize(h, w);
            d.clear();

            List<AttributedString> list = new ArrayList<>();
            for (int i = 0; i < 10000; ++i) {
                list.add(gen(i));
                if (list.size() >= h) {
                    list.remove(0);
                }
                d.resize(h, w);
                d.update(new ArrayList<>(list), size.cursorPos(list.size() - 1,0));
                terminal.flush();
                Thread.sleep(1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            exit();
        }
    }

    public void test3() {
        try {
            init(true);
            log("test3");

            Display d = new Display(terminal, true);

            d.resize(h, w);
            d.clear();

            NonBlockingReader reader = terminal.reader();

            List<AttributedString> list = new ArrayList<>();
            for (int i = 0; i < 10; ++i) {
                list.add(gen(i));

                while (list.size() >= h) {
                    list.remove(0);
                }
                if (changed) {
                    d.clear();
                }
                d.resize(h, w);
                d.update(new ArrayList<>(list), size.cursorPos(list.size() - 1,0));
                terminal.flush();

                log("read " + reader.read(10000));
                Thread.yield();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            exit();
        }
    }

    public void test4() {
        try {
            init(true);
            log("test4");

            Display d = new Display(terminal, true);

            List<AttributedString> list = new ArrayList<>();
            for (int i = 0; i < h; ++i) {
                AttributedString s = new AttributedStringBuilder()
                        .append(Integer.toString(i))
                        .toAttributedString();
                list.add(s);
            }
            d.clear();

            d.resize(h, w);
            d.update(list, size.cursorPos(h - 1, 0));
            terminal.flush();

            terminal.reader().read();

            list = new ArrayList<>(list);
            list.remove(list.size() - 2);
            list.add(0, new AttributedStringBuilder()
                    .append("/")
                    .toAttributedString());

            d.update(list, 0);
            terminal.flush();

            terminal.reader().read();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            exit();
        }
    }
}
