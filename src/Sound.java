import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sound {
    private static Log log = new Log(); // 静态日志实例

    public static void play_sound(String path) {
        log.AddLog(Log.INFO, "[Sound] Starting to play sound - path: '" + path + "'");

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            String threadName = "SoundThread-" + path + "-" + Thread.currentThread().getId();
            Thread.currentThread().setName(threadName);

            log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Sound playback thread started");

            try {
                String fullPath = "Star/sound/" + path + ".wav";
                File soundFile = new File(fullPath);

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Looking for sound file: " + fullPath);

                // 检查文件是否存在
                if (!soundFile.exists()) {
                    log.AddLog(Log.ERROR, "[Sound/" + threadName + "] Sound file not found: " + fullPath);
                    return;
                }

                // 检查文件大小
                long fileSize = soundFile.length();
                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Sound file size: " + fileSize + " bytes");

                if (fileSize == 0) {
                    log.AddLog(Log.WARNING, "[Sound/" + threadName + "] Sound file is empty: " + fullPath);
                    return;
                }

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Opening audio input stream");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

                AudioFormat format = audioInputStream.getFormat();
                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Audio format: " +
                        format.getChannels() + " channels, " +
                        format.getSampleRate() + " Hz, " +
                        format.getSampleSizeInBits() + " bits, " +
                        format.getEncoding().toString());

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Getting audio line");
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Opening audio line");
                line.open(format);

                log.AddLog(Log.INFO, "[Sound/" + threadName + "] Starting audio playback");
                line.start();

                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                long totalBytesRead = 0;
                int chunksRead = 0;

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Beginning audio data transfer");

                while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                    line.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    chunksRead++;

                    // 每读取一定数量的块记录一次进度（避免过于频繁的日志）
                    if (chunksRead % 50 == 0) {
                        log.AddLog(Log.TRACE, "[Sound/" + threadName + "] Playback progress: " +
                                chunksRead + " chunks, " + totalBytesRead + " bytes");
                    }
                }

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Audio data transfer complete - " +
                        chunksRead + " chunks, " + totalBytesRead + " bytes total");

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Draining audio line");
                line.drain();

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Closing audio line");
                line.close();

                log.AddLog(Log.DEBUG, "[Sound/" + threadName + "] Closing audio input stream");
                audioInputStream.close();

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                log.AddLog(Log.INFO, "[Sound/" + threadName + "] Sound playback completed successfully - " +
                        "path: '" + path + "', duration: " + duration + "ms");

            } catch (UnsupportedAudioFileException e) {
                long errorTime = System.currentTimeMillis();
                long duration = errorTime - startTime;

                log.AddERRORLog("[Sound] Unsupported audio file format - path: '" + path +
                        "', thread: " + threadName + ", duration: " + duration + "ms", e);
                Thread.currentThread().interrupt();

            } catch (IOException e) {
                long errorTime = System.currentTimeMillis();
                long duration = errorTime - startTime;

                log.AddERRORLog("[Sound] IO error while playing sound - path: '" + path +
                        "', thread: " + threadName + ", duration: " + duration + "ms", e);
                Thread.currentThread().interrupt();

            } catch (LineUnavailableException e) {
                long errorTime = System.currentTimeMillis();
                long duration = errorTime - startTime;

                log.AddERRORLog("[Sound] Audio line unavailable - path: '" + path +
                        "', thread: " + threadName + ", duration: " + duration + "ms", e);
                Thread.currentThread().interrupt();

            } catch (SecurityException e) {
                long errorTime = System.currentTimeMillis();
                long duration = errorTime - startTime;

                log.AddERRORLog("[Sound] Security exception while accessing audio - path: '" + path +
                        "', thread: " + threadName + ", duration: " + duration + "ms", e);
                Thread.currentThread().interrupt();

            } catch (IllegalArgumentException e) {
                long errorTime = System.currentTimeMillis();
                long duration = errorTime - startTime;

                log.AddERRORLog("[Sound] Invalid argument in audio processing - path: '" + path +
                        "', thread: " + threadName + ", duration: " + duration + "ms", e);
                Thread.currentThread().interrupt();

            } catch (Exception e) {
                long errorTime = System.currentTimeMillis();
                long duration = errorTime - startTime;

                log.AddERRORLog("[Sound] Unexpected error during sound playback - path: '" + path +
                        "', thread: " + threadName + ", duration: " + duration + "ms", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // 添加静态方法来检查声音文件是否存在
    public static boolean soundExists(String path) {
        String fullPath = "Star/sound/" + path + ".wav";
        File soundFile = new File(fullPath);
        boolean exists = soundFile.exists() && soundFile.length() > 0;

        log.AddLog(Log.DEBUG, "[Sound] Checking sound existence - path: '" + path +
                "', fullPath: " + fullPath + ", exists: " + exists);

        return exists;
    }

    // 添加静态方法来获取声音文件信息
    public static void logSoundInfo(String path) {
        String fullPath = "Star/sound/" + path + ".wav";
        File soundFile = new File(fullPath);

        if (!soundFile.exists()) {
            log.AddLog(Log.WARNING, "[Sound] Sound file not found for info: " + fullPath);
            return;
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioInputStream.getFormat();

            log.AddLog(Log.INFO, "[Sound] Sound file info - path: '" + path + "'");
            log.AddLog(Log.INFO, "[Sound]   File size: " + soundFile.length() + " bytes");
            log.AddLog(Log.INFO, "[Sound]   Channels: " + format.getChannels());
            log.AddLog(Log.INFO, "[Sound]   Sample rate: " + format.getSampleRate() + " Hz");
            log.AddLog(Log.INFO, "[Sound]   Sample size: " + format.getSampleSizeInBits() + " bits");
            log.AddLog(Log.INFO, "[Sound]   Encoding: " + format.getEncoding());
            log.AddLog(Log.INFO, "[Sound]   Frame size: " + format.getFrameSize() + " bytes");
            log.AddLog(Log.INFO, "[Sound]   Frame rate: " + format.getFrameRate() + " frames/sec");

            // 计算大致时长
            long frames = audioInputStream.getFrameLength();
            double duration = (frames / format.getFrameRate());
            log.AddLog(Log.INFO, "[Sound]   Estimated duration: " + String.format("%.2f", duration) + " seconds");

            audioInputStream.close();

        } catch (Exception e) {
            log.AddERRORLog("[Sound] Error getting sound file info - path: '" + path + "'", e);
        }
    }

    // 添加性能监控方法
    public static void logAudioSystemInfo() {
        try {
            log.AddLog(Log.INFO, "[Sound] Audio System Information");

            // 获取混音器信息
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            log.AddLog(Log.INFO, "[Sound] Available mixers: " + mixerInfos.length);

            for (Mixer.Info mixerInfo : mixerInfos) {
                log.AddLog(Log.DEBUG, "[Sound] Mixer: " + mixerInfo.getName() +
                        " - " + mixerInfo.getDescription());
            }

            // 检查支持的音频文件类型
            AudioFileFormat.Type[] fileTypes = AudioSystem.getAudioFileTypes();
            log.AddLog(Log.INFO, "[Sound] Supported audio file types: " + fileTypes.length);

            for (AudioFileFormat.Type fileType : fileTypes) {
                log.AddLog(Log.DEBUG, "[Sound] File type: " + fileType.getExtension());
            }

        } catch (Exception e) {
            log.AddERRORLog("[Sound] Error getting audio system information", e);
        }
    }

    // 添加资源清理方法（如果需要）
    public static void cleanup() {
        log.AddLog(Log.INFO, "[Sound] Sound system cleanup called");
        // 这里可以添加任何需要的资源清理代码
    }
}