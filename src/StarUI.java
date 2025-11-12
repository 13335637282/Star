import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.sound.sampled.*;

import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

// JACOB imports
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class StarUI extends JFrame {
    public Config config = new Config();
    public Log log = new Log();
    private TestPaper currentTestPaper;
    private ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean isPlaying = false;
    private ArrayList<QuestionComponent> questionComponents = new ArrayList<>();
    private int questionCount = 0;
    private boolean checked = false;
    private ArrayList<Consumer<? super Graphics>> paintComponentActions = new ArrayList<>();
    private HashMap<String,Thread> threads = new HashMap<>();

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);

        for (Consumer<? super Graphics> action : paintComponentActions) {
            action.accept(g);
        }
    }

    // 用于存储当前播放的Clip，以便可以停止
    private Clip currentClip;

    // 内部类定义试卷数据结构
    public static class TestPaper {
        public String title;
        public String time;
        public List<PaperComponent> components;
    }

    public static class PaperComponent {
        public String type = "";
        public TextContent text;
        public JsonArray value;
        public String question = "";
        public int number_of_answers = -1;
        public List<List<String>> options;
        public JsonElement answer;
        public int min = -1;
        public int max = -1;
        public int maximum_chars = -1;
        public JsonElement score;
    }

    public static class TextContent {
        public String text;
        public int font;
        public boolean bold;
        public boolean italic;
    }

    public StarUI() {

        JDialog frame = new JDialog();
        frame.setSize(500,300);
        frame.setLocationRelativeTo(null);
        JLabel title = new JLabel("Star");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font(config.getString("settings.font"),Font.PLAIN,50));
        frame.add(title);
        frame.setVisible(true);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        frame.dispose();

        setSize(500,300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2,1,100,100));
        setVisible(true);

        add(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clearUI();
        mainInterface();
    }

    public void clearUI() {
        this.getContentPane().removeAll();
        revalidate();
        repaint();
    }


    /**
     * 解析时间字符串为毫秒数
     * 支持格式：
     * - 无小数点: 时:分:秒:毫秒, 时:分:秒, 时:分, 分
     * - 有小数点: 时:分:秒.毫秒, 分:秒.毫秒, 秒.毫秒
     * - 字母: 1h, 1m, 1s, 1ms, 1h30m, 1h4ms 等
     */
    private long parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return 0;
        }

        String input = timeStr.trim().toLowerCase();

        // 检查是否包含字母（字母格式）
        if (containsLetters(input)) {
            return parseLetterFormat(input);
        }

        // 检查是否包含小数点（小数点格式）
        if (input.contains(".")) {
            return parseDotFormat(input);
        }

        // 默认冒号格式
        return parseColonFormat(input);
    }

    /**
     * 检查字符串是否包含字母
     */
    private boolean containsLetters(String str) {
        return str.matches(".*[a-z].*");
    }

    /**
     * 解析字母格式时间
     * 格式: 1h, 1m, 1s, 1ms, 1h30m, 1h4ms, 1h30m10s 等
     */
    private long parseLetterFormat(String input) {
        long totalMillis = 0;

        // 移除所有空格
        input = input.replaceAll("\\s+", "");

        // 使用正则表达式匹配数字和单位
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)([hms]|ms)");
        java.util.regex.Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "h":
                    totalMillis += (long)(value * 3600 * 1000);
                    break;
                case "m":
                    totalMillis += (long)(value * 60 * 1000);
                    break;
                case "s":
                    totalMillis += (long)(value * 1000);
                    break;
                case "ms":
                    totalMillis += (long)value;
                    break;
            }
        }

        return totalMillis;
    }

    /**
     * 解析小数点格式时间
     * 格式: 时:分:秒.毫秒, 分:秒.毫秒, 秒.毫秒
     */
    private long parseDotFormat(String input) {
        String[] parts = input.split("\\.");
        if (parts.length != 2) {
            return parseColonFormat(input); // 回退到冒号格式
        }

        String beforeDot = parts[0];
        String afterDot = parts[1];

        // 解析小数点前的部分（时:分:秒 或 分:秒 或 秒）
        long beforeMillis = parseColonFormat(beforeDot);

        // 解析小数点后的部分（毫秒）
        long millisPart = 0;
        if (afterDot.length() == 1) {
            millisPart = Long.parseLong(afterDot) * 100; // 1位 = 百毫秒
        } else if (afterDot.length() == 2) {
            millisPart = Long.parseLong(afterDot) * 10;  // 2位 = 十毫秒
        } else {
            millisPart = Long.parseLong(afterDot.substring(0, Math.min(3, afterDot.length())));
        }

        return beforeMillis + millisPart;
    }

    /**
     * 解析冒号格式时间
     * 格式: 时:分:秒:毫秒, 时:分:秒, 时:分, 分
     */
    private long parseColonFormat(String input) {
        String[] parts = input.split(":");
        long totalMillis = 0;

        if (parts.length == 1) {
            // 只有分钟
            totalMillis = (long)(Double.parseDouble(parts[0]) * 60 * 1000);
        } else if (parts.length == 2) {
            // 时:分
            totalMillis = (long)(Double.parseDouble(parts[0]) * 3600 * 1000) +
                    (long)(Double.parseDouble(parts[1]) * 60 * 1000);
        } else if (parts.length == 3) {
            // 时:分:秒
            totalMillis = (long)(Double.parseDouble(parts[0]) * 3600 * 1000) +
                    (long)(Double.parseDouble(parts[1]) * 60 * 1000) +
                    (long)(Double.parseDouble(parts[2]) * 1000);
        } else if (parts.length == 4) {
            // 时:分:秒:毫秒
            totalMillis = (long)(Double.parseDouble(parts[0]) * 3600 * 1000) +
                    (long)(Double.parseDouble(parts[1]) * 60 * 1000) +
                    (long)(Double.parseDouble(parts[2]) * 1000) +
                    Long.parseLong(parts[3]);
        }

        return totalMillis;
    }

    /**
     * 将毫秒数格式化为易读的时间字符串
     */
    private String formatMillisToTime(long millis) {
        if (millis <= 0) {
            return "00:00.000";
        }

        long hours = millis / (3600 * 1000);
        long minutes = (millis % (3600 * 1000)) / (60 * 1000);
        long seconds = (millis % (60 * 1000)) / 1000;
        long remainingMillis = millis % 1000;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, remainingMillis);
        } else if (minutes > 0) {
            return String.format("%02d:%02d.%03d", minutes, seconds, remainingMillis);
        } else {
            return String.format("%02d.%03d", seconds, remainingMillis);
        }
    }

    public void testInterface() {
        clearUI();
        setTitle("Star - "+config.getLang("test_interface"));
        setLayout(new BorderLayout());
        currentTestPaper = new TestPaper();
        questionComponents = new ArrayList<>();

        // 读取测试试卷
        String testFile = config.getString("test.last_test");
        if (testFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, config.getLang("no_select_test_paper"));
            mainInterface();
            return;
        }

        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(testFile);
            currentTestPaper = gson.fromJson(reader, TestPaper.class);
            reader.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, config.getLang("reading_test_failed")+":" + e.getMessage());
            mainInterface();
            return;
        }

        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 添加标题
        JLabel titleLabel = new JLabel(currentTestPaper.title);
        titleLabel.setFont(new Font(config.getString("settings.font"), Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);

        // 添加时间信息
        if (currentTestPaper.time != null && !currentTestPaper.time.isEmpty()) {
            JLabel timeLabel = new JLabel(config.getLang("time") + currentTestPaper.time);
            threads.put("test_timer",new Thread(() -> {
                long totalMillis = parseTimeString(currentTestPaper.time);
                long startTime = System.currentTimeMillis();
                long remainingMillis = totalMillis;

                while (!checked && remainingMillis > 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingMillis = Math.max(0, totalMillis - elapsed);

                    // 格式化显示时间
                    String formattedTime = formatMillisToTime(remainingMillis);
                    SwingUtilities.invokeLater(() -> {
                        timeLabel.setText(config.getLang("time") + formattedTime);
                    });

                    if (remainingMillis <= 0) {
                        checkAnswers();
                        break;
                    }

                    try {
                        Thread.sleep(100); // 每100ms更新一次，减少CPU占用
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                checked = false;
            }));

            threads.get("test_timer").start();

            timeLabel.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 14));
            timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(timeLabel);
        }


        // 遍历所有组件并渲染
        if (currentTestPaper.components != null) {
            for (int i = 0; i < currentTestPaper.components.size(); i++) {
                PaperComponent component = currentTestPaper.components.get(i);
                renderComponent(mainPanel, component, i);
            }
        }

        scrollPane.setViewportView(mainPanel);
        add(scrollPane,BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        TextButton backButton = new TextButton(config.getLang("return_main_interface"));
        backButton.setAction(ele -> {
            if (ele == TextButton.MouseEvent.Pressed) {
                stopCurrentAudio();
                Sound.play_sound("ui/click_button");
                while (getWidth() >= 300 || getHeight() >= 100) {
                    setSize(getWidth() - getWidth() / 4, getHeight() - getHeight() / 4);
                    setLocationRelativeTo(null);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                mainInterface();
            }
        });
        buttonPanel.add(backButton);
        add(buttonPanel,BorderLayout.SOUTH);

        revalidate();
        repaint();

        for (QuestionComponent q : questionComponents) {
            if (!q.isValid()) {
                checked=true;

                Object x = JOptionPane.showInputDialog(
                        null,
                        config.getLang("test_error"),
                        "Star",
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        new Object[]{config.getLang("return_main_interface"),config.getLang("ignore_risks")},
                        null);
                if (x.equals(config.getLang("return_main_interface"))) {
                    clearUI();
                    mainInterface();
                    break;
                } else {
                    break;
                }
            }
        }

        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void renderComponent(JPanel parent, PaperComponent component, int index) {
        switch (component.type) {
            case "text":
                renderTextComponent(parent, component);
                break;
            case "fill_in_the_blank", "application", "single_choice", "multiple_choice", "true_or_false", "essay":
                QuestionComponent questionComponent = new QuestionComponent(component);
                parent.add(questionComponent);
                parent.add(Box.createRigidArea(new Dimension(0, 10)));
                questionComponents.add(questionComponent);
                questionCount ++;

                break;
            case "listening_comprehension":
                renderListeningComprehension(parent, component);
                break;
            case "submit":
                renderSubmitButton(parent);
                break;
        }
    }

    private void renderTextComponent(JPanel parent, PaperComponent component) {
        if (component.text != null) {
            JLabel label = new JLabel(component.text.text);
            int style = Font.PLAIN;
            if (component.text.bold) style |= Font.BOLD;
            if (component.text.italic) style |= Font.ITALIC;
            label.setFont(new Font(config.getString("settings.font"), style, component.text.font));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(label);
            parent.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }

    private void renderListeningComprehension(JPanel parent, PaperComponent component) {
        JPanel listeningPanel = new JPanel();
        listeningPanel.setLayout(new BoxLayout(listeningPanel, BoxLayout.Y_AXIS));
        listeningPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        listeningPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 播放按钮和停止按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        TextButton playButton = new TextButton(config.getLang("play_listening"));

        playButton.setAction(ele -> {
            if (ele == TextButton.MouseEvent.Pressed && !isPlaying) {
                if (component.value != null && !component.value.isEmpty()) {
                    String textToSpeak = component.value.get(0).getAsString();
                    playTextWithPauses(textToSpeak);
                }
            }
        });

        buttonPanel.add(playButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        listeningPanel.add(buttonPanel);
        parent.add(listeningPanel);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void playTextWithPauses(String text) {
        audioExecutor.execute(() -> {
            isPlaying = true;
            try {
                // 按标点符号分割文本
                String[] segments = text.split("(?<=[。.,，…、])");

                for (String segment : segments) {
                    if (!isPlaying) break;

                    // 根据标点符号确定停顿时间
                    double pauseSeconds = 0.5; // 默认停顿
                    if (segment.endsWith("。") || segment.endsWith(".")) {
                        pauseSeconds = 0.7;
                    } else if (segment.endsWith("，") || segment.endsWith(",") || segment.endsWith("…")) {
                        pauseSeconds = 0.5;
                    } else if (segment.endsWith("、") || segment.endsWith("...")) {
                        pauseSeconds = 0.4;
                    }

                    // 使用JACOB播放当前片段
                    speakTextWithJacob(segment.trim());

                    // 停顿
                    if (pauseSeconds > 0 && isPlaying) {
                        Thread.sleep((long)(pauseSeconds * 1000));
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, config.getLang("playback_failed")+": " + e.getMessage());
                e.printStackTrace();
            } finally {
                isPlaying = false;
            }
        });
    }

    /**
     * 使用JACOB库进行文本转语音并播放[citation:1][citation:3][citation:5]
     */
    private void speakTextWithJacob(String text) {
        try {
            // 创建临时WAV文件
            File tempFile = File.createTempFile("speech", ".wav");
            tempFile.deleteOnExit();

            String tempFilePath = tempFile.getAbsolutePath();

            // 使用JACOB生成语音文件[citation:7][citation:10]
            ActiveXComponent ax = new ActiveXComponent("Sapi.SpVoice");
            Dispatch spVoice = ax.getObject();

            // 设置语音属性
            ax.setProperty("Volume", new Variant(100)); // 音量 0-100[citation:5]
            ax.setProperty("Rate", new Variant(1));    // 语速 -10 到 +10[citation:5]

            // 创建文件流对象
            ActiveXComponent fileStream = new ActiveXComponent("Sapi.SpFileStream");
            Dispatch spFileStream = fileStream.getObject();

            // 创建音频格式对象
            ActiveXComponent audioFormat = new ActiveXComponent("Sapi.SpAudioFormat");
            Dispatch spAudioFormat = audioFormat.getObject();

            // 设置音频格式为WAV
            Dispatch.put(spAudioFormat, "Type", new Variant(22)); // WAV格式

            // 设置文件流的格式
            Dispatch.putRef(spFileStream, "Format", spAudioFormat);

            // 打开文件流
            Dispatch.call(spFileStream, "Open", new Variant(tempFilePath), new Variant(3), new Variant(true));

            // 将语音输出重定向到文件流
            Dispatch.putRef(spVoice, "AudioOutputStream", spFileStream);

            // 执行朗读到文件
            Dispatch.call(spVoice, "Speak", new Variant(text));

            // 关闭文件流
            Dispatch.call(spFileStream, "Close");
            Dispatch.putRef(spVoice, "AudioOutputStream", null);

            // 释放资源
            spAudioFormat.safeRelease();
            spFileStream.safeRelease();
            spVoice.safeRelease();
            ax.safeRelease();
            fileStream.safeRelease();
            audioFormat.safeRelease();

            // 使用Java自带功能播放生成的WAV文件[citation:2][citation:4][citation:6]
            playWavFile(tempFilePath);

        } catch (Exception e) {
            log.AddERRORLog("PlayBack Failed",e);
        }
    }

    /**
     * 使用Java Sound API播放WAV文件[citation:2][citation:4]
     */
    private void playWavFile(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // 存储当前clip以便可以停止
            currentClip = clip;

            clip.start();

            // 等待播放完成
            while (!clip.isRunning()) {
                Thread.sleep(10);
            }
            while (clip.isRunning()) {
                Thread.sleep(10);
            }

            clip.close();
            audioInputStream.close();
            currentClip = null;

        } catch (Exception e) {
            log.AddERRORLog("playback failed",e);
        }
    }

    /**
     * 停止当前播放的音频
     */
    private void stopCurrentAudio() {
        isPlaying = false;
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    private void renderSubmitButton(JPanel parent) {
        JPanel submitPanel = new JPanel();
        submitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        TextButton submitButton = new TextButton(config.getLang("submit"));
        submitButton.setAction(ele -> {
            if (ele == TextButton.MouseEvent.Pressed && !checked) {
                checkAnswers();
            }
        });
        submitPanel.add(submitButton);

        parent.add(submitPanel);
        parent.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private void checkAnswers() {
        if (!checked ){StringBuilder result = new StringBuilder();

            int correct_blank = calculateScore();
            int total_blank = calculateTotalQuestions();

            int total_score = 0;
            int score = 0;
            for (QuestionComponent comp : questionComponents) {
                total_score += comp.getMax_score();
                score += comp.getScore();
            }
            float score_percentage = (float) (score) / (total_score) * 100.00f;

// 确定评级文本
            String gradeText;
            if (score_percentage >= 95f) { gradeText = "A++"; }
            else if (score_percentage >= 90f) { gradeText = "A+"; }
            else if (score_percentage >= 85f) { gradeText = "A"; }
            else if (score_percentage >= 80f) { gradeText = "B++"; }
            else if (score_percentage >= 75f) { gradeText = "B+"; }
            else if (score_percentage >= 70f) { gradeText = "B"; }
            else if (score_percentage >= 65f) { gradeText = "C+"; }
            else if (score_percentage >= 60f) { gradeText = "C"; }
            else if (score_percentage >= 55f) { gradeText = "D+"; }
            else if (score_percentage >= 50f) { gradeText = "D"; }
            else if (score_percentage >= 45f) { gradeText = "D-"; }
            else if (score_percentage >= 40f) { gradeText = "E+"; }
            else if (score_percentage >= 35f) { gradeText = "E"; }
            else if (score_percentage >= 30f) { gradeText = "E-"; }
            else if (score_percentage >= 25f) { gradeText = "E--"; }
            else if (score_percentage >= 20f) { gradeText = "F++"; }
            else if (score_percentage >= 15f) { gradeText = "F+"; }
            else if (score_percentage >= 10f) { gradeText = "F"; }
            else if (score_percentage >= 5f) { gradeText = "F-"; }
            else { gradeText = "F--"; }

// 确定颜色
            Color gradeColor;
            if (score_percentage >= 85f) {
                gradeColor = Color.green;
            } else if (score_percentage >= 70f) {
                gradeColor = Color.yellow;
            } else if (score_percentage >= 60f) {
                gradeColor = Color.orange;
            } else {
                gradeColor = Color.red;
            }

            result.append(config.getLang("submit_test_paper_msg")
                    .formatted(
                            total_blank,
                            correct_blank,
                            (correct_blank) / (total_blank) * 100f,
                            score,
                            total_score,
                            score_percentage
                    )
            );

// 显示详细结果
            JFrame frame = new JFrame();
            frame.setResizable(false);
            frame.setSize(500, 400); // 稍微增大窗口以容纳印章
            frame.setTitle(config.getLang("result"));
            frame.setAlwaysOnTop(true);
            frame.setLocationRelativeTo(null);
            frame.setLayout(null);

            JTextArea area = new JTextArea(result.toString());
            area.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 15));
            area.setEnabled(false);
            area.setAlignmentX(Component.CENTER_ALIGNMENT);
            area.setBounds(50, 50, 300, 200);
            area.setBackground(new Color(0,0,0,0));

// 创建印章面板
            StampPanel stampPanel = new StampPanel(gradeText, gradeColor);
            stampPanel.setBounds(350, 250, 100, 100);

            frame.add(stampPanel);
            frame.add(area);
            frame.setVisible(true);

            checked = true;

            frame.repaint();
            frame.revalidate();

        }

    }

    private int calculateScore() {
        // 计算总题数
        if (currentTestPaper.components == null) return 0;

        AtomicInteger count = new AtomicInteger();
        for (QuestionComponent comp : questionComponents) {
            comp.check();
            comp.getAnswer().forEach(ele -> {
                if (ele) {
                    count.getAndIncrement();
                }
            });
            log.AddLog(Log.INFO,"comp:"+comp+" total:"+count);
        }
        return count.get();
    }

    private int calculateTotalQuestions() {
        // 计算总题数
        if (currentTestPaper.components == null) return 0;

        int count = 0;

        for (QuestionComponent comp : questionComponents) {
            int a = comp.getAnswerCount();
            count += a;
            log.AddLog(Log.INFO,"comp:"+comp+" answerCount:"+a+" total:"+count);
        }

        return count ;
    }




    public void mainInterface() {
        setLayout(null);
        clearUI();
        setTitle("Star");
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        try {
            threads.get("test_timer").interrupt();
            log.AddLog(Log.INFO,"[StarUI/Threads] Killed Thread!(id:test_timer)");
        } catch (Exception e) {

        }

// 创建一个专门用于绘制波浪的面板
        JPanel wavePanel = new JPanel() {
            Wave[] waves = new Wave[3];

            {
                // 初始化波浪
                waves[0] = new Wave(0.02, 0.4, 0.005, new Color(100, 100, 255, 100), 0.4, 50);
                waves[1] = new Wave(0.03, 0.3, 0.007, new Color(100, 100, 255, 100), 0.6, 50);
                waves[2] = new Wave(0.025, 0.35, 0.004, new Color(100, 100, 255, 100), 0.5, 50);

                Timer timer = new Timer(30, e -> {
                    for (Wave wave : waves) {
                        wave.update();
                    }
                    repaint();
                });
                timer.start();

                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (Wave wave : waves) {
                    wave.draw(g2d, getWidth(), getHeight());
                }
            }
        };

        wavePanel.setLayout(new BorderLayout());

        JLabel subtitle = new JLabel("");
        if (!config.getString("test.last_test").isEmpty()) {
            subtitle.setText(config.getLang("current_test_paper") +
                    config.getString("test.last_test"));
        }

        ActionButton select_test_paper = new ActionButton(
                config.getLang("select_test_paper"),
                30
        );

        select_test_paper.setAction(ele -> {
            if (ele == ActionButton.MouseEvent.Pressed) {
                Sound.play_sound("ui/click_button");
                log.AddLog(Log.INFO, "[Event] Select Test Paper");

                JFileChooser fileChooser = new JFileChooser();
                for (FileFilter fileFilter : fileChooser.getChoosableFileFilters()) {
                    log.AddLog(Log.INFO, "[Event/UI] Removed FileFilter :" + fileFilter);
                    fileChooser.removeChoosableFileFilter(fileFilter);
                }
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || (f.getName().toLowerCase().endsWith(".json"));
                    }

                    @Override
                    public String getDescription() {
                        return
                                config.getString("lang." +
                                        config.getString("settings.lang") +
                                        ".star_test_paper_file");
                    }
                });

                setExtendedState(JFrame.ICONIFIED);

                int r = fileChooser.showOpenDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    subtitle.setText(
                            config.getString("lang." +
                                    config.getString("settings.lang") +
                                    ".current_test_paper") + selectedFile.getPath()
                    );
                    config.setString("test.last_test", selectedFile.getPath());
                }

                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        ActionButton start_paper = new ActionButton(
                config.getString("lang." +
                        config.getString("settings.lang") +
                        ".start_test"),
                30
        );

        start_paper.setAction(ele -> {
            if (ele == ActionButton.MouseEvent.Pressed) {
                Sound.play_sound("ui/click_button");
                while (true) {
                    if (getWidth() < 300 && getHeight() < 100) {
                        break;
                    }
                    setSize(getWidth() - getWidth() / 4, getHeight() - getHeight() / 4);
                    setLocationRelativeTo(null);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                testInterface();
            }
        });

        ActionButton settings = new ActionButton(
                config.getString("lang." +
                        config.getString("settings.lang") +
                        ".settings"),
                30
        );

        settings.setAction(ele -> {
            if (ele.equals(ActionButton.MouseEvent.Pressed)) {
                Sound.play_sound("ui/click_button");
            }
        });

        JPanel titles = new JPanel(new GridLayout(2,1));

        JLabel title = new JLabel("Star");

        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 70));

        subtitle.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 30));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        add(title);
        add(subtitle);
        add(wavePanel);
        add(settings);
        add(select_test_paper);
        add(start_paper);

        Timer timer = new Timer(20, ele ->{
            title.setSize(132,55);
            title.setLocation((getWidth()-title.getWidth())/2,30);
            subtitle.setSize(subtitle.getText().length()*30,55);
            subtitle.setLocation((getWidth()-subtitle.getWidth())/2,100);
            wavePanel.setSize(getWidth(),getHeight()/4);
            wavePanel.setLocation(0,getHeight()/4*3);

            start_paper.setArc(50);
            start_paper.setBounds(
                    (getWidth()-400)/2,300,400,50);

            settings.setArc(50);
            settings.setBounds(
                    (getWidth()-400)/2,400,400,50);

            select_test_paper.setArc(50);
            select_test_paper.setBounds(
                    (getWidth()-400)/2,500,400,50);

        });
        timer.start();


        revalidate();
        repaint();
    }

    @Override
    public void dispose() {
        // 关闭时释放资源
        stopCurrentAudio();
        if (audioExecutor != null && !audioExecutor.isShutdown()) {
            audioExecutor.shutdown();
        }
        super.dispose();
    }
}