import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RadioButton extends JComponent {
    private boolean selected = false;
    private String text;
    private RadioGroup group;
    private int arc = 20;
    private Config config = new Config();
    private Consumer<Boolean> onStateChange;
    private float animationProgress = 0f;
    private Timer animationTimer;
    private boolean hovered = false;
    private boolean pressed = false;


    // 颜色配置 - 浅色主题
    private Color lightBg = new Color(245, 245, 245);
    private Color lightText = new Color(60, 60, 60);
    private Color lightBorder = new Color(180, 180, 180);
    private Color lightHover = new Color(230, 230, 230);
    private Color lightPressed = new Color(210, 210, 210);

    // 颜色配置 - 深色主题
    private Color darkBg = new Color(45, 45, 45);
    private Color darkText = new Color(220, 220, 220);
    private Color darkBorder = new Color(100, 100, 100);
    private Color darkHover = new Color(65, 65, 65);
    private Color darkPressed = new Color(85, 85, 85);

    // 强调色
    private Color accentColor = new Color(0, 122, 255);
    private Color accentHover = new Color(30, 144, 255);
    private Color accentPressed = new Color(0, 100, 220);


    public RadioButton(String text, RadioGroup group) {
        this.text = text;
        this.group = group;
        setPreferredSize(new Dimension(140, 32));
        setMinimumSize(new Dimension(120, 28));
        initEvent();
        setupAnimation();
        group.add(this);
    }

    public RadioButton(String text, RadioGroup group, boolean selected) {
        this(text, group);
        if (selected) {
            setSelected(true);
        }
    }

    private void initEvent() {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()){
                    pressed = true;

                    if (!selected) {
                        setSelected(true);
                        if (onStateChange != null) {
                            onStateChange.accept(true);
                        }
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()){
                    pressed = false;
                }
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }
        });
    }

    private void setupAnimation() {
        animationTimer = new Timer(5, e -> {
            if (selected) {
                animationProgress = Math.min(1f, animationProgress + 0.12f);
            } else {
                animationProgress = Math.max(0f, animationProgress - 0.12f);
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
        Color bgColor, textColor, borderColor, hoverColor, pressedColor;
        if (isDark) {
            bgColor = darkBg;
            textColor = darkText;
            borderColor = darkBorder;
            hoverColor = darkHover;
            pressedColor = darkPressed;
        } else {
            bgColor = lightBg;
            textColor = lightText;
            borderColor = lightBorder;
            hoverColor = lightHover;
            pressedColor = lightPressed;
        }

        // 绘制背景
        if (pressed) {
            g2.setColor(pressedColor);
        } else if (hovered) {
            g2.setColor(hoverColor);
        }
        if (pressed || hovered) {
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        }

        int circleSize = 18;
        int circleX = 10;
        int circleY = (getHeight() - circleSize) / 2;

        // 绘制外圈
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawOval(circleX, circleY, circleSize, circleSize);

        // 绘制选中状态和动画
        if (animationProgress > 0) {
            // 脉动效果
            float pulse = 0.9f + 0.1f * (float) Math.sin(System.currentTimeMillis() * 0.008);

            // 外圈高亮
            Color currentAccent = getCurrentAccentColor();
            g2.setColor(new Color(
                    currentAccent.getRed(),
                    currentAccent.getGreen(),
                    currentAccent.getBlue(),
                    (int)(200 * animationProgress)
            ));
            g2.setStroke(new BasicStroke(2.2f * animationProgress));
            g2.drawOval(circleX, circleY, circleSize, circleSize);

            // 内圆动画 - 缩放和淡入
            float innerScale = 0.4f + 0.4f * animationProgress;
            int innerSize = (int) (circleSize * 0.5f * innerScale);
            int innerX = Math.toIntExact(Math.round(circleX + (circleSize - innerSize) / 2.0d));
            int innerY = Math.toIntExact(Math.round(circleY + (circleSize - innerSize) / 2.0d));

            g2.setColor(currentAccent);
            g2.fillOval(innerX, innerY, innerSize, innerSize);

            // 光晕效果
            if (animationProgress > 0.5f) {
                float glowAlpha = (animationProgress - 0.5f) * 0.4f;
                g2.setColor(new Color(
                        currentAccent.getRed(),
                        currentAccent.getGreen(),
                        currentAccent.getBlue(),
                        (int)(80 * glowAlpha)
                ));
                int glowSize = (int)(circleSize * (1 + 0.3f * animationProgress));
                int glowX = circleX - (glowSize - circleSize) / 2;
                int glowY = circleY - (glowSize - circleSize) / 2;
                g2.fillOval(glowX, glowY, glowSize, glowSize);
            }
        }

        // 绘制文本
        g2.setColor(textColor);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
        FontMetrics fm = g2.getFontMetrics();
        int textX = circleX + circleSize + 12;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    private Color getCurrentAccentColor() {
        if (pressed) {
            return accentPressed;
        } else if (hovered) {
            return accentHover;
        } else {
            return accentColor;
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            if (selected) {
                // 取消同组其他单选框的选中状态
                for (RadioButton radio : group.getRadioButtons()) {
                    if (radio != this && radio.group.equals(this.group)) {
                        radio.setSelected(false);
                    }
                }
            }

            this.selected = selected;
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }
            repaint();

            if (onStateChange != null) {
                onStateChange.accept(selected);
            }
        }
    }

    public RadioGroup getGroup() {
        return group;
    }

    public void setGroup(RadioGroup group) {
        this.group = group;
    }

    public void setOnStateChange(Consumer<Boolean> onStateChange) {
        this.onStateChange = onStateChange;
    }


    // 清理资源
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
}