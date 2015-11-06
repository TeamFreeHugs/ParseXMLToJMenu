import com.eyeball.jmenu.parser.JMenuToXMLParser;

import javax.swing.*;
import java.io.IOException;

public class Test {

    public static void main(String[] a) throws IOException {
        JMenuBar menu = JMenuToXMLParser.parseXML(ClassLoader.getSystemResourceAsStream("test.xml"));
        JFrame f = new JFrame();
        f.setJMenuBar(menu);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
