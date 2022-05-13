package is.bobbys;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public final class Photo implements Serializable {
    public static long limit = 432000L;
    public static Photo mainPhoto;
    private static DesktopFiles des;
    public static void init(DesktopFiles des){
        Photo.des=des;
        try {
            mainPhoto= ((Photo) DesktopFiles.read(".fileManager/Record.obj"));
        } catch (Exception e) {
            mainPhoto=new Photo(des.listOther());
        }
        try {
            DesktopFiles.write(mainPhoto,".fileManager/Record.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void takePhoto(){
        mainPhoto.appendPhoto(new Photo(des.listOther()));
        try {
            DesktopFiles.write(mainPhoto,".fileManager/Record.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(mainPhoto);
    }
    public final HashMap<String, Time> data;

    public Photo(HashMap<String, Time> data) {
        this.data = data;
    }

    public Photo(File[] files) {
        data = new HashMap<>();
        long t = System.currentTimeMillis();
        for (File f : files) {
            data.put(f.getName(),new Time(t,f.lastModified()));
        }
    }

    public void appendPhoto(Photo photo) {
        LinkedList<Object> temp=new LinkedList<>();
        for (String f : data.keySet()) {
            if (!photo.data.containsKey(f))
                temp.add(f);
        }
        for (Object o:temp){
            data.remove(o);
        }

        for (String s:data.keySet()){
            data.get(s).update(photo.data.get(s));
            photo.data.remove(s);
        }

        for (String s:photo.data.keySet()){
            data.put(s,photo.data.get(s));
        }
    }
    public boolean isOld(File f){
        return System.currentTimeMillis()-data.get(f.getName()).getTime()>limit;
    }

    @Override
    public String toString() {
        return "Photo{data=" + data +
                '}';
    }

    private final static class Time implements Externalizable {
        private long sys;
        private long lastMod;

        public Time(long sys, long lastMod) {
            this.sys = sys;
            this.lastMod = lastMod;
        }

        public Time() {
        }

        public long getTime() {
            return Math.max(sys, lastMod);
        }

        public void update(Time time) {
            if (time.lastMod > lastMod) lastMod = time.lastMod;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(sys);
            out.writeLong(lastMod);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            sys=in.readLong();
            lastMod=in.readLong();
        }

        @Override
        public String toString() {
            return "Time{" +
                    "sys=" + new Date(sys) +
                    ", lastMod=" + new Date(lastMod) +
                    '}';
        }
    }
}