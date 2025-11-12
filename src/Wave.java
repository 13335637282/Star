import java.awt.*;
import java.awt.geom.GeneralPath;

public class Wave {
    private double frequency; // 频率
    private double amplitude; // 振幅
    private double speed;     // 速度
    private Color color;      // 颜色
    private double phase;     // 相位
    private double[] points;  // 波浪点
    private int POINT_COUNT = 0;
    private Log log = new Log(); // 添加日志实例

    public Wave(double frequency, double amplitude, double speed, Color color, double phase, int point_count) {
        log.AddLog(Log.INFO, "[Wave] Initializing wave with frequency: " + frequency +
                ", amplitude: " + amplitude + ", speed: " + speed +
                ", color: " + color + ", phase: " + phase + ", point_count: " + point_count);

        this.frequency = frequency;
        this.amplitude = amplitude;
        this.speed = speed;
        this.color = color;
        this.phase = phase;
        this.POINT_COUNT = point_count;
        this.points = new double[point_count];

        // 验证参数
        if (frequency <= 0) {
            log.AddLog(Log.WARNING, "[Wave] Frequency is <= 0: " + frequency);
        }
        if (amplitude <= 0) {
            log.AddLog(Log.WARNING, "[Wave] Amplitude is <= 0: " + amplitude);
        }
        if (point_count <= 0) {
            log.AddLog(Log.WARNING, "[Wave] Point count is <= 0: " + point_count);
        }

        log.AddLog(Log.INFO, "[Wave] Wave initialized successfully");
    }

    public void update() {

        long startTime = System.nanoTime();

        // 更新波浪点
        for (int i = 0; i < POINT_COUNT; i++) {
            double x = (double) i / (POINT_COUNT - 1);
            points[i] = Math.sin(2 * Math.PI * (x + phase)) * amplitude;

        }
        phase += speed;

        long endTime = System.nanoTime();
    }

    public void draw(Graphics2D g2d, int width, int height) {

        long startTime = System.nanoTime();

        // 检查图形上下文状态
        if (g2d == null) {
            return;
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        try {
            GeneralPath path = new GeneralPath();

            // 开始路径
            path.moveTo(0, height);

            // 添加波浪点
            int pointsDrawn = 0;
            for (int i = 0; i < POINT_COUNT; i++) {
                double x = (double) i / (POINT_COUNT - 1) * width;
                double y = height - height / 1.5 + points[i] * height / 1.5;

                // 检查坐标是否有效
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                path.lineTo(x, y);
                pointsDrawn++;

                // 记录关键点的位置用于调试
                if (i == 0 || i == POINT_COUNT / 2 || i == POINT_COUNT - 1) {
                }
            }


            // 闭合路径
            path.lineTo(width, height);
            path.closePath();

            // 绘制波浪
            g2d.setColor(color);
            g2d.fill(path);
        } catch (Exception e) {
            log.AddERRORLog("[Wave] Error during wave drawing", e);
        }
    }

    // 添加getter方法用于调试和监控
    public double getFrequency() {
        return frequency;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public double getSpeed() {
        return speed;
    }

    public Color getColor() {
        return color;
    }

    public double getPhase() {
        return phase;
    }

    // 添加setter方法用于动态调整波浪参数
    public void setFrequency(double frequency) {
        log.AddLog(Log.INFO, "[Wave] Frequency changed from " + this.frequency + " to " + frequency);
        this.frequency = frequency;
    }

    public void setAmplitude(double amplitude) {
        log.AddLog(Log.INFO, "[Wave] Amplitude changed from " + this.amplitude + " to " + amplitude);
        this.amplitude = amplitude;
    }

    public void setSpeed(double speed) {
        log.AddLog(Log.INFO, "[Wave] Speed changed from " + this.speed + " to " + speed);
        this.speed = speed;
    }

    public void setColor(Color color) {
        log.AddLog(Log.INFO, "[Wave] Color changed from " + this.color + " to " + color);
        this.color = color;
    }

    public void setPhase(double phase) {
        log.AddLog(Log.INFO, "[Wave] Phase changed from " + this.phase + " to " + phase);
        this.phase = phase;
    }

    // 添加重置方法
    public void reset() {
        log.AddLog(Log.INFO, "[Wave] Resetting wave parameters");
        this.phase = 0;
        // 重置点数组
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i] = 0;
        }
        log.AddLog(Log.INFO, "[Wave] Wave reset completed");
    }

    // 添加性能监控方法
    public void logPerformanceMetrics() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        for (int i = 0; i < POINT_COUNT; i++) {
            if (points[i] < min) min = points[i];
            if (points[i] > max) max = points[i];
            sum += points[i];
        }

        double average = sum / POINT_COUNT;

        log.AddLog(Log.INFO, "[Wave] Performance Metrics - Min: " + String.format("%.3f", min) +
                ", Max: " + String.format("%.3f", max) +
                ", Avg: " + String.format("%.3f", average) +
                ", Phase: " + String.format("%.3f", phase));
    }

    @Override
    public String toString() {
        return String.format("Wave[freq=%.3f, amp=%.3f, speed=%.3f, phase=%.3f, points=%d]",
                frequency, amplitude, speed, phase, POINT_COUNT);
    }
}