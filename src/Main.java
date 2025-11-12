import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Main {
    public static Config config = new Config();
    public static Log log = new Log();
    public static void main(String[] args) {
        log.AddLog(Log.INFO,"[Main] Installing Theme");
        if (config.getString("settings.ui.theme").equals("dark")) {
            if (config.getString("settings.ui.os").equals("mac")) {
                FlatMacDarkLaf.setup();
                log.AddLog(Log.INFO,"[Main] Installing Mac Dark Theme");
            } else {
                FlatDarkLaf.setup();
                config.setString("settings.ui.os","windows");
                log.AddLog(Log.INFO,"[Main] Installing Dark Theme");
            }
        } else {
            config.setString("settings.ui.theme","light");
            if (config.getString("settings.ui.os").equals("mac")) {
                FlatMacLightLaf.setup();
                log.AddLog(Log.INFO,"[Main] Installing Mac Light Theme");
            } else {
                FlatLightLaf.setup();
                config.setString("settings.ui.os","windows");
                log.AddLog(Log.INFO,"[Main] Installing Light Theme");
            }
        }

        // 检查完整Star文件完整性
        if (!new File("Star/jacob-1.18-x64.dll").exists() || !new File("Star/jacob-1.18-x64.dll").exists()) {
            // 下载地址 https://github.com/freemansoft/jacob-project/releases/download/Root_B-1_18/jacob-1.18.zip
            // zip 结构
            // jacob-1.18
            //  |_ jacob-1.18-x64.dll
            //  |_ jacob-1.18-x86.dll
            // TODO 如果没有dll，就下载zip到Star/Temp，然后解压到Star/Temp，自动选择系统架构(x64/x86)，复制到Star文件夹，然后删除zip和解压文件夹
            // TODO 复制完成后，根据系统架构复制到 C:\Windows\System32  或  C:\Windows\SysWOW64

        }

        if (!new File("Star/sound/ui/click_button.wav").exists()) {
            if (new File("Star/sound/ui").mkdirs()) {
                
            }
        }

        StarUI starUI = new StarUI();
        log.AddLog(Log.INFO,"[Main] Start UI");
    }
}