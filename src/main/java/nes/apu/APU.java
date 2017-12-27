package nes.apu;

import javax.sound.sampled.*;

public class APU {

    private SourceDataLine line;

    public APU() { }

    private static final int SAMPLE_RATE = 44100; // 44.1KHz

    public void reset() throws LineUnavailableException {
        // オーディオ形式を指定
        AudioFormat audio_format = new AudioFormat(
                SAMPLE_RATE, 8, 1, true, true);
        DataLine.Info info = new DataLine.Info(
                SourceDataLine.class, audio_format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open();
        line.start();
    }

    public void runStep() {
        // バイト列に適当な矩形波を作成
//
//        int frequency = 440;
//        byte[] b = new byte[SAMPLE_RATE];
//        for (int i = 0; i < b.length; i++) {
//            int r = i / (SAMPLE_RATE / frequency);
//            b[i] = (byte)((r % 2 == 0) ? 100 : -100);
//        }
//        line.write(b, 0, b.length);
//        line.drain(); // 終了まで待機
    }
}
