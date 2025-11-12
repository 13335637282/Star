import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sound {
    public static void play_sound(String path) {
        new Thread(() -> {
            try {
                File soundFile = new File("Star/sound/" + path + ".wav");

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

                AudioFormat format = audioInputStream.getFormat();
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);

                line.start();
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                    line.write(buffer, 0, bytesRead);
                }

                line.drain();
                line.close();
                audioInputStream.close();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
