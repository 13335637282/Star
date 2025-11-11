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

    public Wave(double frequency, double amplitude, double speed, Color color, double phase, int point_count) {
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.speed = speed;
        this.color = color;
        this.phase = phase;
        this.points = new double[point_count];
        POINT_COUNT = point_count;
    }

    public void update() {
        // 更新波浪点
        for (int i = 0; i < POINT_COUNT; i++) {
            double x = (double) i / (POINT_COUNT - 1);
            points[i] = Math.sin(2 * Math.PI * (x + phase)) * amplitude;
        }
        phase += speed;
    }

    public void draw(Graphics2D g2d, int width, int height) {
        GeneralPath path = new GeneralPath();

        // 开始路径
        path.moveTo(0, height);

        // 添加波浪点
        for (int i = 0; i < POINT_COUNT; i++) {
            double x = (double) i / (POINT_COUNT - 1) * width;
            double y = height - height / 1.5 + points[i] * height / 1.5;
            path.lineTo(x, y);
        }

        // 闭合路径
        path.lineTo(width, height);
        path.closePath();

        // 绘制波浪
        g2d.setColor(color);
        g2d.fill(path);
    }
}