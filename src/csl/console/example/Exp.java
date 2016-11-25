package csl.console.example;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Exp {
    public static void main(String[] args) throws Exception {
        new Exp().test1();
    }

    Terminal terminal;
    long time;
    int w;
    int h;
    Size size;

    private void init(boolean set) throws Exception {
        terminal = TerminalBuilder.terminal();
        if (set) {
            terminal.enterRawMode();

            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_xmit);

            terminal.puts(InfoCmp.Capability.clear_screen);
        }

        w = Math.max(terminal.getWidth(), 10);
        h = Math.max(terminal.getHeight(), 10);
        size = terminal.getSize();
        time = System.nanoTime();
    }

    public void test1()  {
        try {
            init(true);
            /////////////

            //terminal.writer().append("hello " + w + " " + gen(10) + ":");
            //terminal.puts(InfoCmp.Capability.carriage_return);

            terminal.flush();


            for (int i = 0; i < 10000; ++i) {
                terminal.writer().append(gen(i));

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

    private void exit() {
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.flush();

        long diff = System.nanoTime() - time;
        System.out.println("time=" + (diff / 1000_000) + " " + w + "," + h + " : " + terminal.getClass());
    }

    private String gen(int s) {
        int v = 0xF0F0F0;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; buf.length() < w; ++i) {
            v = v * 3 + (i + s);
            String n = Integer.toString(v) + "!";
            if (buf.length() + n.length() >= w) {
                break;
            }
            buf.append(n);
            //System.err.println("<" + Integer.toString(v) + ">");
        }
        return buf.substring(0, Math.min(w, buf.length()));
    }

    public void test2() {
        try {
            init(true);

            Display d = new Display(terminal, true);

            d.resize(h, w);
            d.clear();

            List<AttributedString> list = new ArrayList<>();
            for (int i = 0; i < 10000; ++i) {
                list.add(AttributedStringBuilder.append(gen(i), ""));
                if (list.size() >= h) {
                    list.remove(0);
                }
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
}
