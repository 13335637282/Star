import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.io.*;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.fusesource.jansi.Ansi;

public class Log {
    static final String Information = "INFO";
    static final String INFO= "INFO";
    static final String WARNING = "WARNING";
    static final String ERROR = "ERROR";
    static final String DEBUG = "DEBUG";
    static final String TRACE = "TRACE";
    static String log_path = "Star/log";
    static Config config = new Config();
    static {
        if (new File(config.getString("log.path")).exists()) {
            log_path = config.getString("log.path");
        } else {
            if (new File(config.getString("log.path")).mkdirs()) {
                log_path = config.getString("log.path");
            }
        }
    }
    public Log() {
    }

    public String AddLog(String level, String content) {
        String text =
                "[" +
                level +
                "] " +
                "(" +
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) +
                "| " + System.currentTimeMillis() + ") " +
                content;

        try {
            new File(log_path).mkdirs();
            FileWriter bufferedWriter =
                            new FileWriter(
                                    log_path+"\\Log" +
                                            new SimpleDateFormat("yyyy").format(new Date())+".log", true);
            bufferedWriter.append(text).append("\n");
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            //Log File Write Error Code : 1002 写入日志文件错误
            JOptionPane.showMessageDialog(
                    null,
                    "[SERIOUS ERROR] 写入日志文件错误 错误代码:1002",
                    "SERIOUS ERROR",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1002);
        }

        switch (level) {
            case INFO -> System.out.println(new Ansi().fg(Ansi.Color.BLUE).bold().a(text).reset());
            case WARNING -> System.out.println(new Ansi().fg(Ansi.Color.YELLOW).bold().a(text).reset());
            case ERROR -> System.out.println(new Ansi().fg(Ansi.Color.RED).bold().a(text).reset());
            case DEBUG -> System.out.println(new Ansi().fg(Ansi.Color.CYAN).bold().a(text).reset());
            case TRACE -> System.out.println(new Ansi().fg(Ansi.Color.MAGENTA).bold().a(text).reset());
        }


        return text;
    }

    public String AddERRORLog(String content, Exception ot) {
        String text =
                "[" +
                        ERROR +
                        "] " +
                        "(" +
                        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) +
                        ") " +
                        content +
                        " ||  Message: " + ot.getMessage() +
                        " | StackTrace :" + Arrays.toString(ot.getStackTrace()) +
                        " | Cause: " + ot.getCause();

        try {
            FileWriter bufferedWriter =
                    new FileWriter(
                                    "RNFL\\Log\\Log" +
                                    new SimpleDateFormat("yyyy").format(new Date())+".log", true);
            bufferedWriter.append(text).append("\n");
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            //Log File Write Error Code : 1002 写入日志文件错误
            JOptionPane.showMessageDialog(
                    null,
                    "[SERIOUS ERROR] 写入日志文件错误 错误代码:1002",
                    "SERIOUS ERROR",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1002);
        }
        System.out.println(new Ansi().fg(Ansi.Color.RED).bold().a(text).reset());
        return text;
    }
}
