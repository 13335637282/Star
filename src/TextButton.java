import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class TextButton extends JLabel {
    private int PressedFontSize = 17;
    private int ReleasedFontSize = 16;
    private int EnteredFontSize = 16;
    private int ExitedFontSize = 15;
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
        public void accept(MouseEvent string) {
            return;
        }
    };

    public TextButton(String text,boolean big){
        setText(text);
        init_event();
        setHorizontalAlignment(SwingConstants.CENTER);

        if (big) {
            PressedFontSize = 37;
            ReleasedFontSize = 35;
            EnteredFontSize = 35;
            ExitedFontSize = 30;
        }

        setFont(new Font(config.getString("settings.font"),Font.PLAIN,ExitedFontSize));
    }

    public TextButton(String text){
        setText(text);
        init_event();
        setFont(new Font(config.getString("settings.font"),Font.PLAIN,ExitedFontSize));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    public TextButton(String text, int enteredFontSize, int exitedFontSize, int pressedFontSize, int releasedFontSize) {
        this.EnteredFontSize = enteredFontSize;
        this.ReleasedFontSize = releasedFontSize;
        this.ExitedFontSize = exitedFontSize;
        this.PressedFontSize = pressedFontSize;
        setText(text);
        init_event();
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(new Font(config.getString("settings.font"),Font.PLAIN,ExitedFontSize));
        setBackground(new Color(0,0,0,0));
    }


    public void init_event() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.accept(MouseEvent.Clicked);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!(PressedFontSize == -1)) {
                    setFont(new Font(config.getString("settings.font"),Font.BOLD,PressedFontSize));
                }
                action.accept(MouseEvent.Pressed);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (!(ReleasedFontSize == -1)) {
                    setFont(new Font(config.getString("settings.font"),Font.BOLD,ReleasedFontSize));
                }
                action.accept(MouseEvent.Released);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!(EnteredFontSize == -1)) {
                    setFont(new Font(config.getString("settings.font"),Font.BOLD,EnteredFontSize));
                };
                action.accept(MouseEvent.Entered);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!(ExitedFontSize == -1)) {
                    setFont(new Font(config.getString("settings.font"),Font.PLAIN,ExitedFontSize));
                }
                action.accept(MouseEvent.Exited);
            }
        });
    }

    public int getPressedFontSize() {
        return PressedFontSize;
    }

    /**
     @param pressedFontSize set -1 = disable
     */
    public void setPressedFontSize(int pressedFontSize) {
        PressedFontSize = pressedFontSize;
    }


    public int getReleasedFontSize() {
        return ReleasedFontSize;
    }

    /**
     @param releasedFontSize set -1 = disable
     */
    public void setReleasedFontSize(int releasedFontSize) {
        ReleasedFontSize = releasedFontSize;
    }



    public int getEnteredFontSize() {
        return EnteredFontSize;
    }

    /**
     @param enteredFontSize set -1 = disable
     */
    public void setEnteredFontSize(int enteredFontSize) {
        EnteredFontSize = enteredFontSize;
    }



    public int getExitedFontSize() {
        return ExitedFontSize;
    }

    /**
     @param exitedFontSize set -1 = disable
     */
    public void setExitedFontSize(int exitedFontSize) {
        ExitedFontSize = exitedFontSize;
    }

    public Consumer<? super MouseEvent> getAction() {
        return action;
    }

    public void setAction(Consumer<? super MouseEvent> action) {
        this.action = action;
    }
}
