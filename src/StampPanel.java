import javax.swing.*;
import java.awt.*;

class StampPanel extends JPanel {
    private String text;
    private Color color;
    private Config config = new Config();

    public StampPanel(String text, Color color) {
        this.text = text;
        this.color = color;
        setOpaque(false); // 设置透明背景
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int diameter = Math.min(getWidth(), getHeight()) - 10;
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        // 绘制圆形边框
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawOval(x, y, diameter, diameter);

        // 绘制文本
        g2d.setColor(color);
        g2d.setFont(new Font(config.getString("settings.font"), Font.BOLD, 30));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int textX = (getWidth() - textWidth) / 2;
        int textY = (getHeight() + textHeight / 2) / 2;

        g2d.drawString(text, textX, textY);
    }
}