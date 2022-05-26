package is.bobbys;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class ControlPane extends JFrame {
    private final DesktopFiles desktop=new DesktopFiles();
    public ControlPane() {
        super("桌面管理器");
        Photo.init(desktop);
        setResizable(false);
        setBounds(500,500,500,250);
        Container c=getContentPane();
        c.setLayout(null);
        JButton path=new JButton("设置环境变量");
        JButton clean=new JButton("立即清理(自动忽略*.lnk)");
        JButton addIg=new JButton("添加忽略文件（不存在的会自动删除）");
        JButton openOld=new JButton("打开旧文件文件夹");
        JButton rnAll=new JButton("批量重命名");
        JButton name=new JButton("Name.jar Support");
        JButton exit=new JButton("退出程序");
        JButton uDisk=new JButton("UDisk");
        JButton setTime=new JButton("设置清理时间：1天");
        c.add(path).setBounds(0,0,250,30);
        c.add(clean).setBounds(250,0,250,30);
        c.add(addIg).setBounds(0,30,250,30);
        c.add(openOld).setBounds(250,30,250,30);
        c.add(rnAll).setBounds(0,60,250,30);
        c.add(name).setBounds(250,60,250,30);
        c.add(exit).setBounds(0,90,250,30);
        c.add(uDisk).setBounds(250,90,250,30);
        c.add(setTime).setBounds(0,120,250,30);
        path.addActionListener(e -> desktop.setPath());
        clean.addActionListener(e -> desktop.doClean());
        addIg.addActionListener(e -> desktop.addIgnore());
        openOld.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(".fileManager/Old Files"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        rnAll.addActionListener(e -> Names.renameAll(Main.chooseDir("选择文件夹")));
        name.addActionListener(e -> desktop.openName());
        exit.addActionListener(e -> System.exit(0));
        uDisk.addActionListener(e -> {
            File[] rs=File.listRoots();
            File target=new File(".fileManager/UDISK");
            if (!target.exists()) target.mkdir();
            String t=new SimpleDateFormat("MM-dd hh:mm:ss ").format(new Date());
            for (File r:rs){
                if (r.getAbsolutePath().contains("C")||r.getAbsolutePath().contains("D")||r.getAbsolutePath().contains("E")) {
                    System.out.println("pass:"+r);
                    continue;
                }
                try {
                    DesktopFiles.copy(r,new File(target,t+r.getName()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        setTime.addActionListener(e -> {
            String s=JOptionPane.showInputDialog("输入时间：（天）");
            if (s==null) return;
            try {
                double d=Double.parseDouble(s);
                if (d<0) return;
                Photo.limit= (long) (d*86400000);
                setTime.setText("设置清理时间："+d+"天");
            }catch (NumberFormatException ignored){
            }
        });
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        try {
            Image img = ImageIO.read(getClass().getClassLoader().getResource("res/img.jpeg"));
            setIconImage(img);
            TrayIcon trayIcon=new TrayIcon(img.getScaledInstance(16,16,Image.SCALE_SMOOTH),"FileManager");
            SystemTray.getSystemTray().add(trayIcon);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setVisible(!isVisible());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(()->{
            while (true) {
                desktop.doClean();
                try {
                    Thread.sleep(3600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
