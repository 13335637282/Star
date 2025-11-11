import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class ActionButton extends JButton {
    private int arc = 15; // 圆角弧度，默认15
    public static enum MouseEvent {
        Pressed,
        Released,
        Entered,
        Exited,
        Clicked
    }
    private Config config = new Config();
    private Consumer<? super MouseEvent> action = new Consumer<MouseEvent>() {
        @Override
        public void accept(MouseEvent event) {
            return;
        }
    };

    public ActionButton(String text){
        setText(text);
        init_event();
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    public ActionButton(String text, int arc) {
        this.arc = arc;
        setText(text);
        setForeground(new Color(0,0,0));
        init_event();
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    public void init_event() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.accept(MouseEvent.Clicked);
                repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                action.accept(MouseEvent.Pressed);
                repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                action.accept(MouseEvent.Released);
                repaint();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                action.accept(MouseEvent.Entered);
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                action.accept(MouseEvent.Exited);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 根据鼠标状态选择颜色
        if (getModel().isRollover()) {
            // 鼠标悬停时使用半透明淡蓝色
            g2.setColor(new Color(173, 216, 230, 150)); // 淡蓝色，半透明
        } else {
            // 默认状态使用半透明白色
            g2.setColor(new Color(255, 255, 255, 100)); // 白色，半透明
        }

        // 绘制圆角矩形背景
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        g2.dispose();

        // 绘制文本
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        // 不绘制边框，因为我们自定义了背景
    }

    public int getArc() {
        return arc;
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    public void setAction(Consumer<? super MouseEvent> action) {
        this.action = action;
    }
}