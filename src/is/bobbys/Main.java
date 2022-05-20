package is.bobbys;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class Main {
    public static void main(String...args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
//        test();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Names.init();
        new ControlPane();
    }
    public static File chooseDir(String title){
        JFileChooser c=new JFileChooser();
        c.setDialogTitle(title);
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return c.getSelectedFile();
        }else {
            return null;
        }
    }
    public static void test() throws IOException {
        System.exit(0);
    }
}
