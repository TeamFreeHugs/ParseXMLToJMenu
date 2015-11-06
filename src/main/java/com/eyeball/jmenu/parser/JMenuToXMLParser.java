package com.eyeball.jmenu.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("ALL")
public class JMenuToXMLParser {

    public static JMenuBar parseXML(InputStream inputStream) throws IOException {
        StringBuilder html = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        for (String s = reader.readLine(); s != null; s = reader.readLine())
            html.append(s).append("\n");
        return parseXML(html.toString());
    }

    public static JMenuBar parseXML(String html) throws IOException, ParseException {
        JMenuBar root = new JMenuBar();
        Document document = Jsoup.parse(html);
        if (document.select("html body jmenubar").size() == 0) {
            throw new ParseException("Child element must be a <jmenubar>");
        }
        Element rootElement = document.select("html body jmenubar").get(0);
        Elements jmenus = document.select("html body jmenubar jmenu");
        Elements allIfs = document.select("if");
        for (Element ifElement : allIfs) {
            if (ifElement.attr("type").isEmpty())
                throw new ParseException("If statement without a type!");
            if (ifElement.attr("type").equals("os")) {
                String os = System.getProperty("os.name").toLowerCase();
                lookForOSes:
                {
                    for (Element child : ifElement.children()) {
                        if (child.tagName().equals("windows") && os.contains("window") || child.tagName().equals("mac") && os.contains("mac") || child.tagName().equals("linux") && os.contains("linux") || child.tagName().equals("other")) {
                            Elements allChildren = Jsoup.parse(child.html()).select("html body").get(0).children();
                            //Somehow it's flipped?
                            //Collections.reverse(allChildren);
                            //Eh anyway when we parse the <event><!--Handlers--></event>, it is fipped, so don't bother.
                            for (Element foundChild : allChildren) {
                                ifElement.after(foundChild);
                            }
                            ifElement.remove();
                            break lookForOSes;
                        }
                    }
                }
            }
        }
        System.out.println(document.ownerDocument().html());
        for (Element jmenu : jmenus) {
            if (jmenu.parent().equals(document.select("html body jmenubar").get(0))) {
                JMenu menu = createJMenu(jmenu);
                root.add(menu);
            }
        }
        return root;
    }

    private static JMenu createJMenu(Element jmenu) {
        String name = jmenu.select("info name").get(0).text();
        JMenu menu = new JMenu(name);
        String mnemonic = jmenu.select("info mnemonic").get(0).text();
        if (!mnemonic.isEmpty()) {
            try {
                int keyVal = KeyEvent.class.getField(mnemonic).getInt(null);
                menu.setMnemonic(keyVal);
            } catch (Exception e) {
                throw new ParseException("Could not get mnemonic value of JMenu! Value: " + mnemonic);
            }
        }
        Elements children = jmenu.select("children jmenuitem, children group, children separator, children jmenu");
        ArrayList<Element> seen = new ArrayList<Element>();
        int lastSeperatorID = 0;
        for (Element child : children) {
            if (seen.contains(child))
                continue;
            if (!child.parent().equals(jmenu.select("children").get(0))) {
                continue;
            }
            seen.add(child);
            if (child.tagName().equals("group")) {
                ButtonGroup group = new ButtonGroup();
                handleGroupItems(menu, child, group);
            } else if (child.tagName().equals("jmenuitem")) {
                String itemName = child.select("text").text();
                JMenuItem item = new JMenuItem(itemName);
                setupAccelerator(child, item);
                setupEventListeners(child, item);
                if (child.select("icon").size() != 0) {
                    try {
                        URL url = new URL(child.select("icon").text());
                        item.setIcon(new ImageIcon(ImageIO.read(url)));
                    } catch (MalformedURLException e) {
                        try {
                            item.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(child.select("icon").text()))));
                        } catch (Exception e1) {
                            throw new ParseException("Could not read image file: " + child.select("icon").text());
                        }
                    } catch (Exception e) {
                        throw new ParseException("Could not read image URL: " + child.select("icon").text());
                    }
                }
                menu.add(item);
            } else if (child.tagName().equals("separator")) {
                menu.addSeparator();
                seen.remove(child);
                child.attr("data-seperator-id", "" + lastSeperatorID);
                seen.add(child);
                lastSeperatorID++;
            } else if (child.tagName().equals("jmenu")) {
                menu.add(createJMenu(child));
            }
        }
        return menu;
    }

    private static void handleGroupItems(JMenu menu, Element child, ButtonGroup group) {
        for (Element groupChild : child.select("radiobutton, checkbox")) {
            String itemName = groupChild.select("text").text();
            JMenuItem item;
            if (groupChild.tagName().equals("radiobutton")) {
                item = new JRadioButtonMenuItem(itemName);
            } else if (groupChild.tagName().equals("checkbox")) {
                item = new JCheckBoxMenuItem(itemName);
            } else {
                throw new ParseException("Unknown child " + groupChild.tagName());
            }
            if (groupChild.select("selected").size() != 0)
                item.setSelected(Boolean.parseBoolean(groupChild.select("selected").text()));
            if (groupChild.select("icon").size() != 0) {
                try {
                    URL url = new URL(groupChild.select("icon").text());
                    item.setIcon(new ImageIcon(ImageIO.read(url)));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    try {
                        item.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(groupChild.select("icon").text()))));
                    } catch (IOException e1) {
                        throw new ParseException("Could not read image file: " + groupChild.select("icon").text());
                    }
                } catch (IOException e) {
                    throw new ParseException("Could not read image URL: " + groupChild.select("icon").text());
                }
            }
            setupAccelerator(groupChild, item);
            setupEventListeners(groupChild, item);
            group.add(item);
            menu.add(item);
        }
    }

    private static void setupEventListeners(Element child, JMenuItem item) {
        if (child.select("events").size() != 0) {
            for (Element action : child.select("events").get(0).children()) {
                try {
                    if (!action.attr("className").isEmpty()) {
                        final Method toRun = Class.forName((action.attr("packageName").isEmpty() ? "" : action.attr("packageName") + ".") + action.attr("className")).getMethod(action.attr("methodName"), ActionEvent.class);
                        if (action.tagName().equals("click")) {
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        toRun.invoke(null, e);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        }
                    } else if (action.attr("eval").toLowerCase().equals("true")) {
                        String language = action.attr("lang");
                        final String eval = action.attr("run");
                        final ScriptEngine engine = new ScriptEngineManager().getEngineByName(language);

                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    engine.eval(eval);
                                } catch (ScriptException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    throw new ParseException("Could not find method: " + e.getMessage());
                }
            }
        }
    }

    private static void setupAccelerator(Element child, JComponent item) {
        if (child.select("accelerator").size() != 0) {
            Element accelerator = child.select("accelerator").get(0);
            int key = 0, mask = 0;
            if (accelerator.select("key").text().isEmpty())
                throw new ParseException("Could not get accelerator key value of JMenu!");
            if (accelerator.select("mask").text().isEmpty())
                throw new ParseException("Could not get accelerator mask value of JMenu!");
            try {
                key = KeyEvent.class.getField(accelerator.select("key").text()).getInt(null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ParseException("Could not get accelerator key value of JMenu! Value: " + accelerator.select("key").text());
            }
            for (Element m : accelerator.select("mask")) {
                try {
                    mask += ActionEvent.class.getField(m.text() + "_MASK").getInt(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ParseException("Could not get accelerator mask value of JMenu! Value: " + m.text() + "_MASK");
                }
            }

            try {
                item.getClass().getMethod("setAccelerator", KeyStroke.class).invoke(item, KeyStroke.getKeyStroke(key, mask));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (child.select("mnemonic").size() != 0) {
            Element mnemonic = child.select("mnemonic").get(0);
            int key = 0;
            if (mnemonic.text().isEmpty())
                throw new ParseException("Could not get mnemonic value of JMenu!");
            try {
                key = KeyEvent.class.getField(mnemonic.text()).getInt(null);
            } catch (Exception e) {
                throw new ParseException("Could not get mnemonic value of JMenu! Value: " + mnemonic.text());
            }
            try {
                ((AbstractButton) item).setMnemonic(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}