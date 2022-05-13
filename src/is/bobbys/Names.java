package is.bobbys;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public final class Names {
    private static final HashSet<String> set=new HashSet<>();
    public static void init(){
        try (var w=new BufferedReader(new InputStreamReader(new FileInputStream(".fileManager/Names.txt"), StandardCharsets.UTF_8))){
            String l=w.readLine();
            while (l!=null){
                set.add(l);
                l= w.readLine();
            }
        }catch (FileNotFoundException notF){
            try {
                new File(".fileManager/Names.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static String getContainedN(String name){
        for (String s:set){
            if (name.contains(s))
                return s+ name.substring(name.lastIndexOf("."));
        }
        return name;
    }
    public static void renameAll(File file){
        if (file==null) return;
        String s;
        for (File f:file.listFiles()){
            s=getContainedN(f.getName());
            if (!s.equals(f.getName()))
                f.renameTo(new File(file,s));
        }
    }
}
