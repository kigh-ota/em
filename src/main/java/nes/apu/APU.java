package nes.apu;

import common.ByteRegister;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.util.Random;

@Slf4j
public class APU {

    private SourceDataLine line;

    public final ByteRegister regSQ1_VOL = new ByteRegister((byte)0); // $4000
    public final ByteRegister regSQ1_SWEEP = new ByteRegister((byte)0); // $4001
    public final ByteRegister regSQ1_LO = new ByteRegister((byte)0); // $4002
    public final ByteRegister regSQ1_HI = new ByteRegister((byte)0); // $4003
    public final ByteRegister regSQ2_VOL= new ByteRegister((byte)0); // $4004
    public final ByteRegister regSQ2_SWEEP = new ByteRegister((byte)0); // $4005
    public final ByteRegister regSQ2_LO = new ByteRegister((byte)0); // $4006
    public final ByteRegister regSQ2_HI = new ByteRegister((byte)0); // $4007
    public final ByteRegister regTRI_LINEAR = new ByteRegister((byte)0); // $4008
    public final ByteRegister regUNUSED1 = new ByteRegister((byte)0); // $4009
    public final ByteRegister regTRI_LO = new ByteRegister((byte)0); // $400A
    public final ByteRegister regTRI_HI = new ByteRegister((byte)0); // $400B
    public final ByteRegister regNOISE_VOL = new ByteRegister((byte)0); // $400C
    public final ByteRegister regUNUSED2 = new ByteRegister((byte)0); // $400D
    public final ByteRegister regNOISE_LO = new ByteRegister((byte)0); // $400E
    public final ByteRegister regNOISE_HI = new ByteRegister((byte)0); // $400F
    public final ByteRegister regDMC_FREQ = new ByteRegister((byte)0); // $4010
    public final ByteRegister regDMC_RAW = new ByteRegister((byte)0); // $4011
    public final ByteRegister regDMC_START = new ByteRegister((byte)0); // $4012
    public final ByteRegister regDMC_LEN = new ByteRegister((byte)0); // $4013
    public final ByteRegister regSND_CHN = new ByteRegister((byte)0); // $4015

    public APU() { }

    private static final int SAMPLE_RATE = 22050;

    public void reset() throws LineUnavailableException {
        // オーディオ形式を指定
        AudioFormat audioFormat = new AudioFormat(
                SAMPLE_RATE, 8, 1, false, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open();
        line.start();

        cycle = 0;
        sample = 0;
        freqParam = 0.7;
    }

    private Random random = new Random();

    private static final int BASE_FREQ = 1789773; // Hz
    private static final int BUFFER_LENGTH = 110;

    private double freqParam;

    private byte[] buffer = new byte[BUFFER_LENGTH];

    private long cycle;
    private int sample;

    /**
     * 各チャネル
     * - 可変レートタイマー
     * - フレームカウンタからのクロックで駆動される変調器
     *
     * STATUS($4015)で各チャネルをON/OFFできる
     * 各チャネルの出力は非線形ミキシングされる
     *
     * 矩形波・三角波・雑音：
     * - すべてのlength counterが非ゼロのときに波形を再生する
     *
     * 矩形波：
     * - freqがある閾値以上の場合に再生しない
     * - sweep towards lower freq.
     * t = ($4003:2-0)($4002)の11bit
     * freq = 1.789773MHz / (16 * (t + 1))
     *   ただしt<8なら再生しない
     * duty = ($4000:8-7)の2bit
     *   0: 01000000
     *   1: 01100000
     *   2: 01111000
     *   3: 10011111
     * constant volume flag = $4000:4 (0: エンベロープ/1: 定音量)
     * V = $4000:3-0
     * エンベロープ
     *  start flag: offならdividerがクロック
     *            : onならoffにして、decay level counterが15になる
     */
    public void runStep() {
//        final int freq = 440;

        if (cycle % (getBaseFrequency() / SAMPLE_RATE) == 0) {
            int pulse = getPulse1() + getPulse2();
            if (pulse == 0) {
                buffer[sample % BUFFER_LENGTH] = 0;
            } else {
                double outputLevel = 95.88 / ((8128 / pulse) + 100);
                buffer[sample % BUFFER_LENGTH] = (byte) (255 * outputLevel);
            }
            sample++;
            if (sample % BUFFER_LENGTH == 0) {

//                if (line.getBufferSize() - line.available() == 0) {
//                    freqParam -= 0.001;
//                    log.warn("freqParam={}", freqParam);
//                } else {
//                    freqParam += 0.001;
//                    if (freqParam >= 1.0) {
//                        freqParam = 1.0;
//                    }
//                    log.warn("freqParam={}", freqParam);
//                }

                line.write(buffer, 0, BUFFER_LENGTH);
            }
        }

        cycle++;

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

    int getBaseFrequency() {
        return (int)(BASE_FREQ * freqParam);
    }

    /**
     *
     * @return 0-15
     */
    byte getPulse1() {
        int t = (Byte.toUnsignedInt(regSQ1_HI.get()) & 0b00000111) << 8 | Byte.toUnsignedInt(regSQ1_LO.get());
        if (t < 8 || !isPulse1Enabled()) {
            return 0;
        } else {
            int freq = BASE_FREQ / (16 * (t + 1));
            long r = sample / (SAMPLE_RATE / (2 * freq));
            return (byte)((r % 2 == 0) ? 15 : 0);
        }
    }

    byte getPulse2() {
        int t = (Byte.toUnsignedInt(regSQ2_HI.get()) & 0b00000111) << 8 | Byte.toUnsignedInt(regSQ2_LO.get());
        if (t < 8 || !isPulse2Enabled()) {
            return 0;
        } else {
            int freq = BASE_FREQ / (16 * (t + 1));
            long r = sample / (SAMPLE_RATE / (2 * freq));
            return (byte)((r % 2 == 0) ? 15 : 0);
        }
    }

    boolean isPulse1Enabled() {
        return regSND_CHN.getBit(0);
    }

    boolean isPulse2Enabled() {
        return regSND_CHN.getBit(1);
    }

    boolean isTriangleEnabled() {
        return regSND_CHN.getBit(2);
    }

    boolean isNoiseEnabled() {
        return regSND_CHN.getBit(3);
    }

    boolean isDMCEnabled() {
        return regSND_CHN.getBit(4);
    }

}
