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

        StarUI starUI = new StarUI();
        log.AddLog(Log.INFO,"[Main] Start UI");
    }
}