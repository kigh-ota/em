package nes.apu;

import common.ByteRegister;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nes.cpu.CPU;

import javax.sound.sampled.*;
import java.util.Random;

import static nes.apu.APU.FrameCounterMode.FIVE_STEP;
import static nes.apu.APU.FrameCounterMode.FOUR_STEP;

/**
 * APU - Pulse -
 */
@Slf4j
public class APU {

    @Setter
    private CPU cpu;

    private SourceDataLine line;

    public final PulseVolumeRegister regSQ1_VOL; // $4000
    public final PulseSweepRegister regSQ1_SWEEP; // $4001
    public final PulseLowRegister regSQ1_LO; // $4002
    public final PulseHighRegister regSQ1_HI; // $4003
    public final PulseVolumeRegister regSQ2_VOL; // $4004
    public final PulseSweepRegister regSQ2_SWEEP; // $4005
    public final PulseLowRegister regSQ2_LO; // $4006
    public final PulseHighRegister regSQ2_HI; // $4007
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
    public final StatusRegister regAPUSTATUS; // $4015

    public APU() {
        pulse1 = new Pulse();
        pulse2 = new Pulse();

        regSQ1_VOL = new PulseVolumeRegister(pulse1, this);
        regSQ1_SWEEP = new PulseSweepRegister(pulse1.getSweep(), this);
        regSQ1_LO = new PulseLowRegister(pulse1, this);
        regSQ1_HI = new PulseHighRegister(pulse1, this);
        regSQ2_VOL = new PulseVolumeRegister(pulse2, this);
        regSQ2_SWEEP = new PulseSweepRegister(pulse2.getSweep(), this);
        regSQ2_LO = new PulseLowRegister(pulse2, this);
        regSQ2_HI = new PulseHighRegister(pulse2, this);
        regAPUSTATUS = new StatusRegister(pulse1, pulse2, this);
    }

    private static final int SAMPLE_RATE = 22050;

    public void reset() throws LineUnavailableException {
        // オーディオ形式を指定
        AudioFormat audioFormat = new AudioFormat(
                SAMPLE_RATE, 8, 1, false, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open();
        line.start();

        pulse1.reset();
        pulse2.reset();

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

    private final Pulse pulse1;
    private final Pulse pulse2;

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
        runStepInner();
        cycle++;
    }

    private void runStepInner() {

        if (cycle % 2 == 0) {
            pulse1.clockTimer();
            pulse2.clockTimer();
        }

        FrameCounterMode frameCounterMode = getFrameCounterMode();
        if (frameCounterMode == FIVE_STEP) {
            switch ((int) (cycle % frameCounterMode.getCycles())) {
                case 0:
                    // frame interrupt
                    break;
                case 7457:
                    // 1 envelope & triangles's linear
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    break;
                case 14913:
                    // 2 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
                case 22371:
                    // 3 envelope & triangles's linear
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    break;
                case 29828:
                    // frame interrupt
                    break;
                case 29829:
                    // 4 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
            }
        } else {
            switch ((int) (cycle % frameCounterMode.getCycles())) {
                case 7457:
                    // 1 envelope & triangles's linear
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    break;
                case 14913:
                    // 2 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
                case 22371:
                    // 3 envelope & triangles's linear
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    break;
                case 37281:
                    // 4 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
            }
        }

        if (cycle % (getBaseFrequency() / SAMPLE_RATE) == 0) {
            // mix pulse1 & pulse2
            buffer[sample % BUFFER_LENGTH] = mixPulse();
//            log.warn("{}", buffer[sample % BUFFER_LENGTH]);
            sample++;
            if (sample % BUFFER_LENGTH == 0) {
                // when buffer is filled

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
    }

    private byte mixPulse() {
        int pulse = pulse1.get() + pulse2.get();
        if (pulse == 0) {
            return 0;
        }
        double outputLevel = 95.88 / ((8128 / pulse) + 100); // 0.0-1.0
        return (byte)(255 * outputLevel);
    }

    int getBaseFrequency() {
        return (int)(BASE_FREQ * freqParam);
    }

    enum FrameCounterMode {
        FOUR_STEP(29830), FIVE_STEP(37282);

        @Getter
        private final int cycles;

        FrameCounterMode(int cycles) {
            this.cycles = cycles;
        }
    }


    FrameCounterMode getFrameCounterMode() {
        return cpu.regJOY2.getBit(7) ? FIVE_STEP : FOUR_STEP;
    }

    // TODO frame interrupt

}
