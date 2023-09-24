package is.bobbys;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Log {
    private static final StringBuilder logs=new StringBuilder();
    private static final SimpleDateFormat format=new SimpleDateFormat("[HH:mm:ss] ");
    public static void log(String s){
//        System.out.println(s);
        logs.append(format.format(new Date()));
        logs.append(s);
        logs.append('\n');
        if (frame!=null&&frame.isVisible()){
            logs.append(format.format(new Date()));
            textArea.append(s);
            textArea.append("\n");
        }
    }
    public static void err(Exception e){
        e.printStackTrace();
        StackTraceElement[] stacks=e.getStackTrace();
        for (StackTraceElement traceElement:stacks){
            logs.append("\tat ").append(traceElement);
            logs.append('\n');
        }

        if (frame!=null&&frame.isVisible()){
            textArea.setText(logs.toString());
        }
    }
    private static JFrame frame;
    private static JTextArea textArea;
    public static void showFrame(){
        if (frame==null){
            frame=new JFrame("日志");
            frame.setSize(640,480);
            textArea=new JTextArea();
            frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setFont(new Font("微软雅黑",Font.PLAIN,13));
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        textArea.setText(logs.toString());
        frame.setVisible(true);
    }
}
