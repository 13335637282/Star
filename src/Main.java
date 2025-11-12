import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.net.ssl.*;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        if (!new File("Star/jacob-1.18-x64.dll").exists() || !new File("Star/jacob-1.18-x86.dll").exists()) {
            log.AddLog(Log.INFO, "[Main] Jacob DLL files missing, starting download...");
            downloadAndExtractJacob();
        }

        if (!new File("Star/sound/ui/click_button.wav").exists()) {
            log.AddLog(Log.INFO, "[Main] Sound file missing, starting download...");
            downloadSoundFile();
        }

        StarUI starUI = new StarUI();
        log.AddLog(Log.INFO,"[Main] Start UI");
    }


    /**
     * 下载并解压Jacob库
     */
    private static void downloadAndExtractJacob() {
        try {
            // 创建临时目录
            File tempDir = new File("Star/Temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // 使用HTTP链接避免SSL问题
            String zipUrl = "http://github.com/freemansoft/jacob-project/releases/download/Root_B-1_18/jacob-1.18.zip";
            String zipPath = "Star/Temp/jacob-1.18.zip";

            log.AddLog(Log.INFO, "[Main] Downloading Jacob library from: " + zipUrl);
            downloadFile(zipUrl, zipPath);

            // 解压ZIP文件
            log.AddLog(Log.INFO, "[Main] Extracting Jacob library...");
            extractZip(zipPath, "Star/Temp");

            // 根据系统架构复制DLL文件
            String arch = System.getProperty("os.arch").toLowerCase();
            String sourceDll, targetDll;

            if (arch.contains("64")) {
                sourceDll = "Star/Temp/jacob-1.18/jacob-1.18-x64.dll";
                targetDll = "Star/jacob-1.18-x64.dll";
                log.AddLog(Log.INFO, "[Main] Detected 64-bit system, using x64 DLL");
            } else {
                sourceDll = "Star/Temp/jacob-1.18/jacob-1.18-x86.dll";
                targetDll = "Star/jacob-1.18-x86.dll";
                log.AddLog(Log.INFO, "[Main] Detected 32-bit system, using x86 DLL");
            }

            // 复制DLL到Star目录
            Files.copy(Path.of(sourceDll), Path.of(targetDll), StandardCopyOption.REPLACE_EXISTING);
            log.AddLog(Log.INFO, "[Main] Copied DLL to: " + targetDll);

            // 复制DLL到系统目录
            String systemDir = getSystemDirectory();
            String systemDllPath = systemDir + File.separator + new File(targetDll).getName();
            try {
                Files.copy(Path.of(sourceDll), Path.of(systemDllPath), StandardCopyOption.REPLACE_EXISTING);
                log.AddLog(Log.INFO, "[Main] Copied DLL to system directory: " + systemDllPath);
            } catch (Exception e) {
                log.AddLog(Log.WARNING, "[Main] Failed to copy DLL to system directory: " + e.getMessage());
            }

            // 清理临时文件
            deleteDirectory(new File("Star/Temp"));
            log.AddLog(Log.INFO, "[Main] Cleaned up temporary files");

        } catch (Exception e) {
            log.AddLog(Log.ERROR, "[Main] Failed to download and extract Jacob library: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to download required Jacob library: " + e.getMessage(),
                    "Download Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 下载声音文件
     */
    private static void downloadSoundFile() {
        try {
            // 创建声音目录
            File soundDir = new File("Star/sound/ui");
            if (!soundDir.exists()) {
                soundDir.mkdirs();
            }

            // 使用HTTP链接避免SSL问题
            String soundUrl = "http://github.com/13335637282/Star/raw/refs/heads/main/Star/sound/ui/click_button.wav";
            String soundPath = "Star/sound/ui/click_button.wav";

            log.AddLog(Log.INFO, "[Main] Downloading sound file from: " + soundUrl);
            downloadFile(soundUrl, soundPath);
            log.AddLog(Log.INFO, "[Main] Sound file downloaded successfully");

        } catch (Exception e) {
            log.AddLog(Log.ERROR, "[Main] Failed to download sound file: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to download sound file: " + e.getMessage(),
                    "Download Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 下载文件（支持HTTPS和HTTP）
     */
    private static void downloadFile(String fileUrl, String savePath) throws IOException {
        // 如果是HTTPS链接，禁用SSL验证
        if (fileUrl.startsWith("https")) {
            disableSSLVerification();
        }

        URL url = new URL(fileUrl);

        // 使用HttpsURLConnection或HttpURLConnection
        if (fileUrl.startsWith("https")) {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            setupConnection(connection, savePath);
        } else {
            // 对于HTTP链接，使用普通URLConnection
            URLConnection connection = url.openConnection();
            setupConnection(connection, savePath);
        }
    }

    /**
     * 设置连接参数并下载
     */
    private static void setupConnection(URLConnection connection, String savePath) throws IOException {
        // 设置超时时间
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(savePath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // 如果是HttpsURLConnection，断开连接
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).disconnect();
        }
    }

    /**
     * 禁用SSL证书验证
     */
    private static void disableSSLVerification() {
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // 安装信任所有证书的TrustManager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // 创建接受所有主机名的HostnameVerifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // 安装接受所有主机名的HostnameVerifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (Exception e) {
            log.AddLog(Log.WARNING, "[Main] Failed to disable SSL verification: " + e.getMessage());
        }
    }

    /**
     * 解压ZIP文件
     */
    private static void extractZip(String zipPath, String destDir) throws IOException {
        byte[] buffer = new byte[8192];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // 创建父目录
                    new File(newFile.getParent()).mkdirs();

                    // 写入文件
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }

    /**
     * 获取系统目录路径
     */
    private static String getSystemDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (os.contains("windows")) {
            if (arch.contains("64")) {
                // 64位系统，64位DLL应该放到System32，32位DLL应该放到SysWOW64
                return System.getenv("SystemRoot") + "\\System32";
            } else {
                // 32位系统
                return System.getenv("SystemRoot") + "\\System32";
            }
        } else {
            // 非Windows系统，不需要复制到系统目录
            return System.getProperty("user.home");
        }
    }

    /**
     * 递归删除目录
     */
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}