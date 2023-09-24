package is.bobbys;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DesktopFiles {
    private static final class Matrix implements Serializable{
        public final String path;
        public final String matrix;

        public Matrix(String path, String matrix) {
            this.path = path;
            this.matrix = matrix;
        }

        @Override
        public String toString() {
            return "Matrix{" +
                    "path='" + path + '\'' +
                    ", matrix='" + matrix + '\'' +
                    '}';
        }
    }
    public final HashMap<String, String> path;
    private final HashMap<String, Matrix> matrix;
    private final HashSet<String> ignores;
    private File desktop;

    public DesktopFiles() {
        HashMap p;
        try {
            p = (HashMap<String, String>) read(".fileManager/path.obj");
        } catch (IOException | ClassNotFoundException io) {
            p = new HashMap<>();
        }
        path = p;
        try {
            p = (HashMap<String, Matrix>) read(".fileManager/matrix.obj");
        } catch (IOException | ClassNotFoundException io) {
            p = new HashMap<>();
        }
        matrix=p;
        File f;
        String s = path.get("桌面");
        if (s != null && (f = new File(s)).exists()) {
            desktop = f;
        } else {
            desktop = Main.chooseDir("选择桌面文件夹");
            if (desktop == null)
                System.exit(0);
            path.put("桌面", desktop.getAbsolutePath());
        }

        HashSet<String> set;
        try {
            set = (HashSet<String>) read(".fileManager/ignore.obj");
        } catch (IOException | ClassNotFoundException io) {
            set = new HashSet<>();
        }
        ignores = set;

        try {
            File root=new File(".fileManager");
            if (!root.exists()) root.mkdir();
            write(path, ".fileManager/path.obj");
            write(ignores, ".fileManager/ignore.obj");
            write(matrix,".fileManager/matrix.obj");
        } catch (IOException e) {
            Log.err(e);
        }

        File f2=new File(".fileManager/Old Files");
        if (!f2.exists()) f2.mkdir();

        for (File file:f2.listFiles()){
            if (file.isDirectory()&&file.list().length==0) file.delete();
        }
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
                    Log.err(e);
                }
            }else return;
        }
        try {
            Runtime.getRuntime().exec("java -jar "+s+" setting path="+new File(s).getParent()+"\\Name.dat");
        } catch (IOException e) {
            Log.err(e);
        }
    }

    public void addIgnore() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择忽略文件(支持多选)");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) {
                ignores.add(f.getName());
            }
            try {
                write(ignores, ".fileManager/ignore.obj");
            } catch (IOException e) {
                Log.err(e);
            }
        }
    }

    public boolean isIgnored(String name) {
        return ignores.contains(name);
    }

    public File[] listFiles() {
        File[] result=desktop.listFiles(pathname ->
                !(pathname.getName().endsWith(".lnk") || isIgnored(pathname.getName())||pathname.isHidden()));
        return result;
    }

    public void setPath() {
        String s = JOptionPane.showInputDialog("输入类别名称/输入桌面重设桌面");
        if (s == null) return;
        File f = Main.chooseDir("选择文件夹");
        if (f == null) {
            if (!s.equals("桌面")){
                matrix.remove(s);
            }
            return;
        }
        if (s.equals("桌面")){
            path.put(s, f.getAbsolutePath());
            desktop = new File(f.getAbsolutePath());
            try {
                write(path, ".fileManager/path.obj");
            } catch (IOException e) {
                Log.err(e);
            }
        }else {
            String m = JOptionPane.showInputDialog("输入正则表达式");
            if (m == null) return;
            matrix.put(s,new Matrix(f.getAbsolutePath(),m));
            try {
                write(matrix, ".fileManager/matrix.obj");
            } catch (IOException e) {
                Log.err(e);
            }
        }
    }
    public synchronized void doClean() {
        Log.log("Do clean");
        File temp;

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
                Log.err(e);
            }
        }
        Log.log("ignores: "+Arrays.toString(ignores.toArray()));
        Log.log(matrix.toString());
        String n;
        Photo.takePhoto();
        Date d=new Date();
        String date=new SimpleDateFormat("hh_mm_ss@").format(d);
        String dir=new SimpleDateFormat("yyyy-MM-dd").format(d);
        File oldPath=new File(".fileManager/Old Files/"+dir);
        if (!oldPath.exists()) oldPath.mkdir();
        Set<String> matrixKey=matrix.keySet();
        File[] files=listFiles();
        Log.log("Files:"+Arrays.toString(files));
        LoopA:
        for (File f : files) {
            n=f.getName();
            for (String mName:matrixKey){
                if (n.matches(matrix.get(mName).matrix)){
                    try {
                        move(f,new File(matrix.get(mName).path,n));
                    } catch (IOException e) {
                        Log.err(e);
                    }
                    continue LoopA;
                }
            }
            if (Photo.mainPhoto.isOld(f)) {
                try {
                    move(f, new File(oldPath, date+n));
                    Log.log(n+"[cleaned]");
                } catch (IOException e) {
                    Log.err(e);
                }
            }
        }
        Photo.takePhoto();
        Photo.save();
        Log.log(Photo.mainPhoto.toString());
    }
    public static void move(File src,File tar) throws IOException {
        if (tar.exists()) {
            move(src,new File(tar.getParent()+"\\(2)"+tar.getName()));
        }else if (src.isDirectory()){
            tar.mkdir();
            for (File f:src.listFiles()){
                move(f,new File(tar,f.getName()));
            }
            src.delete();
        }else {
            if (src.canWrite()) {
                Files.move(src.toPath(), tar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Log.log(src.getName() + "[move to]" + tar.getAbsolutePath());
            }else {
                Log.log(src.getName()+"[cant write]");
            }
        }
    }
    public static void copy(File src,File tar) throws IOException {
        if (tar.exists()) {
            copy(src,new File(tar.getParent()+"\\(2)"+tar.getName()));
        }else if (src.isDirectory()){
            tar.mkdir();
            for (File f:src.listFiles()){
                copy(f,new File(tar,f.getName()));
            }
            src.delete();
        }else {
            if (src.canWrite()) {
                Files.copy(src.toPath(), tar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}