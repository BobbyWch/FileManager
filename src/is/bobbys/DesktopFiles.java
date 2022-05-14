package is.bobbys;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DesktopFiles {
    private final HashMap<String, String> path;
    private final HashSet<String> ignores;
    private File desktop;

    public DesktopFiles() {
        HashMap<String, String> p;
        try {
            p = (HashMap<String, String>) read(".fileManager/path.obj");
        } catch (IOException | ClassNotFoundException io) {
            p = new HashMap<>();
        }
        path = p;

        File f;
        String s = p.get("桌面");
        if (s != null && (f = new File(s)).exists()) {
            desktop = f;
        } else {
            desktop = Main.chooseDir("选择桌面文件夹");
            if (desktop == null)
                System.exit(0);
            p.put("桌面", desktop.getAbsolutePath());
        }

        HashSet<String> set;
        try {
            set = (HashSet<String>) read(".fileManager/ignore.obj");
        } catch (IOException | ClassNotFoundException io) {
            set = new HashSet<>();
        }
        ignores = set;

        try {
            write(path, ".fileManager/path.obj");
            write(ignores, ".fileManager/ignore.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f2=new File(".fileManager/Old Files");
        if (!f2.exists()) f2.mkdir();
    }

    public static Object read(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(path));
        Object o = os.readObject();
        os.close();
        return o;
    }

    public static void write(Object o, String path) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
        os.writeObject(o);
        os.close();
    }
    public void openName(){
        String s=path.get("name");
        if (s==null){
            JFileChooser chooser=new JFileChooser();
            chooser.setDialogTitle("选择随机点名主程序");
            chooser.setFileFilter(new FileNameExtensionFilter("JAR文件(*.jar)","jar"));
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                s=chooser.getSelectedFile().getAbsolutePath();
                path.put("name",s);
                try {
                    write(path, ".fileManager/path.obj");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else return;
        }
        try {
            Runtime.getRuntime().exec("java -jar "+s+" setting path="+new File(s).getParent()+"\\Name.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addIgnore() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择忽略文件(可一次选择多个)");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) {
                ignores.add(f.getName());
            }
            try {
                write(ignores, ".fileManager/ignore.obj");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isIgnored(String name) {
        return ignores.contains(name);
    }

    public File[] listPPT() {
        System.out.println("PPT:"+ Arrays.toString(desktop.listFiles(pathname ->
                (pathname.getName().endsWith(".pptx") || pathname.getName().endsWith(".ppt")) &&
                        !isIgnored(pathname.getName())&&!pathname.isHidden())));
        return desktop.listFiles(pathname ->
                (pathname.getName().endsWith(".pptx") || pathname.getName().endsWith(".ppt")) &&
                        !isIgnored(pathname.getName())&&!pathname.isHidden());
    }

    public File[] listDir() {
        System.out.println("Dir:"+ Arrays.toString(desktop.listFiles(pathname ->
                pathname.isDirectory() && !isIgnored(pathname.getName())&&!pathname.isHidden())));
        return desktop.listFiles(pathname ->
                pathname.isDirectory() && !isIgnored(pathname.getName())&&!pathname.isHidden());
    }

    public File[] listOther() {
        System.out.println("Other:"+ Arrays.toString(desktop.listFiles(pathname ->
                !(pathname.getName().endsWith(".lnk") || isIgnored(pathname.getName())||pathname.isHidden()))));
        return desktop.listFiles(pathname ->
                !(pathname.getName().endsWith(".lnk") || isIgnored(pathname.getName())||pathname.isHidden()));
    }

    public void setPath() {
        String a = "桌面, 语文, 英语, 历史, name";
        String s = JOptionPane.showInputDialog("输入key(Available: " + a + ')');
        if (s==null) return;
        if (!a.contains(s)) {
            JOptionPane.showMessageDialog(null, "无效key！");
        } else {
            File f = Main.chooseDir("选择文件夹");
            if (f == null)
                return;
            path.put(s, f.getAbsolutePath());
            if (s.equals("桌面")) desktop = new File(f.getAbsolutePath());
        }
        try {
            write(path,".fileManager/path.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void doClean() {
        System.out.println("开始清理");
        File temp, cn, en, his;

        int size= ignores.size();
        Iterator<String> itr= ignores.iterator();
        while (itr.hasNext()) {
            temp = new File(desktop, itr.next());
            if (!temp.exists()||temp.isHidden()) itr.remove();
        }
        if (ignores.size()!=size){
            try {
                write(ignores, ".fileManager/ignore.obj");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ignores: "+Arrays.toString(ignores.toArray()));

        cn = (path.get("语文") == null) ? null : new File(path.get("语文"));
        en = (path.get("英语") == null) ? null : new File(path.get("英语"));
        his = (path.get("历史") == null) ? null : new File(path.get("历史"));
        String newN,n;
        for (File f : listPPT()) {
            n = f.getName();
            newN=Names.getContainedN(n);
            try {
                if (n.equals(newN)){
                    if (n.matches("第\\d{1,2}课.*")) {
                        move(f, new File(his, n));
                    }
                }else {
                    if (n.contains("语文") && cn != null) {
                        move(f, new File(cn, newN));
                    } else if (n.contains("英语") && en != null) {
                        move(f, new File(en, newN));
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        for (File f : listDir()) {
            n = f.getName();
            if (n.matches("第\\d{1,2}课.*")) {
                try {
                    move(f, new File(his, n));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Photo.takePhoto();
        Date d=new Date();
        String date=new SimpleDateFormat("hh_mm_ss@").format(d);
        String dir=new SimpleDateFormat("yyyy-MM-dd").format(d);
        File oldPath=new File(".fileManager/Old Files/"+dir);
        if (!oldPath.exists()) oldPath.mkdir();
        for (File f : listOther()) {
            if (Photo.mainPhoto.isOld(f)) {
                try {
                    move(f, new File(oldPath, date+f.getName()));
                    System.out.println(f.getName()+"【被清理】");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Photo.takePhoto();
        Photo.save();
    }
    public static void move(File src,File tar) throws IOException {
        if (tar.exists()) {
            move(src,new File("(2)"+tar.getAbsolutePath()));
        }else if (src.isDirectory()){
            tar.mkdir();
            for (File f:src.listFiles()){
                move(f,new File(tar,f.getName()));
            }
            src.delete();
        }else {
            if (src.canWrite()) {
                Files.move(src.toPath(), tar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println(src.getName() + "【移动到】" + tar.getAbsolutePath());
            }else {
                System.out.println(src.getName()+"【无法写入】");
            }
        }
    }
}