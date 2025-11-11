import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class CheckBox extends JComponent {
    private boolean selected = false;
    private String text;
    private int arc = 6;
    private Config config = new Config();
    private Consumer<Boolean> onStateChange;
    private float animationProgress = 0f;
    private Timer animationTimer;
    private boolean hovered = false;

    // 颜色配置
    private Color lightBg = new Color(245, 245, 245);
    private Color lightText = new Color(60, 60, 60);
    private Color lightBorder = new Color(200, 200, 200);
    private Color lightHover = new Color(230, 230, 230);

    private Color darkBg = new Color(60, 60, 60);
    private Color darkText = new Color(220, 220, 220);
    private Color darkBorder = new Color(100, 100, 100);
    private Color darkHover = new Color(80, 80, 80);

    private Color accentColor = new Color(0, 122, 255);

    public CheckBox(String text) {
        this.text = text;
        setPreferredSize(new Dimension(120, 30));
        initEvent();
        setupAnimation();
    }

    public CheckBox(String text, boolean selected) {
        this(text);
        this.selected = selected;
        this.animationProgress = selected ? 1f : 0f;
    }

    private void initEvent() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                repaint();

                if (isEnabled()){
                    if (!selected) {
                        setSelected(true);
                        if (onStateChange != null) {
                            onStateChange.accept(true);
                        }
                    } else {
                        setSelected(false);
                        if (onStateChange != null) {
                            onStateChange.accept(true);
                        }

                    }
                }
            }


            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    private void setupAnimation() {
        animationTimer = new Timer(16, e -> {
            if (selected) {
                animationProgress = Math.min(1f, animationProgress + 0.15f);
            } else {
                animationProgress = Math.max(0f, animationProgress - 0.15f);
            }
            repaint();

            if ((selected && animationProgress >= 1f) || (!selected && animationProgress <= 0f)) {
                animationTimer.stop();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isDark = "dark".equals(config.getString("settings.ui.theme"));

        // 设置颜色主题
        Color bgColor, textColor, borderColor, hoverColor;
        if (isDark) {
            bgColor = darkBg;
            textColor = darkText;
            borderColor = darkBorder;
            hoverColor = darkHover;
        } else {
            bgColor = lightBg;
            textColor = lightText;
            borderColor = lightBorder;
            hoverColor = lightHover;
        }

        // 绘制背景
        if (hovered) {
            g2.setColor(hoverColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        }

        int boxSize = 20;
        int boxX = 8;
        int boxY = (getHeight() - boxSize) / 2;

        // 绘制复选框边框
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(boxX, boxY, boxSize, boxSize, arc, arc);

        // 绘制选中动画
        if (animationProgress > 0) {
            // 背景填充动画
            float fillProgress = Math.min(1f, animationProgress * 1.2f);
            Color fillColor = new Color(
                    accentColor.getRed(),
                    accentColor.getGreen(),
                    accentColor.getBlue(),
                    (int)(255 * fillProgress * 0.3f)
            );
            g2.setColor(fillColor);
            g2.fillRoundRect(boxX, boxY, boxSize, boxSize, arc, arc);

            // 对勾动画
            if (animationProgress > 0.3f) {
                float checkProgress = (animationProgress - 0.3f) / 0.7f;
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2.5f));

                // 对勾路径动画
                int startX = boxX + 4;
                int startY = boxY + boxSize / 2;
                int midX = boxX + 8;
                int midY = boxY + boxSize - 6;
                int endX = boxX + boxSize - 4;
                int endY = boxY + 4;

                // 第一部分动画
                if (checkProgress < 0.5f) {
                    float part1Progress = checkProgress * 2;
                    int currentX = (int)(startX + (midX - startX) * part1Progress);
                    int currentY = (int)(startY + (midY - startY) * part1Progress);
                    g2.drawLine(startX, startY, currentX, currentY);
                } else {
                    g2.drawLine(startX, startY, midX, midY);

                    // 第二部分动画
                    float part2Progress = (checkProgress - 0.5f) * 2;
                    int currentX = (int)(midX + (endX - midX) * part2Progress);
                    int currentY = (int)(midY + (endY - midY) * part2Progress);
                    g2.drawLine(midX, midY, currentX, currentY);
                }
            }

            // 边框高亮
            g2.setColor(accentColor);
            g2.setStroke(new BasicStroke(2f * animationProgress));
            g2.drawRoundRect(boxX, boxY, boxSize, boxSize, arc, arc);
        }

        // 绘制文本
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = boxX + boxSize + 8;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }
            repaint();
        }
    }

    public void setOnStateChange(Consumer<Boolean> onStateChange) {
        this.onStateChange = onStateChange;
    }
}