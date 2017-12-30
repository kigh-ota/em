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

@Slf4j
public class APU {

    @Setter
    private CPU cpu;

    private SourceDataLine line;

    private final PulseChannel pulse1;
    private final PulseChannel pulse2;
    private final TriangleChannel triangle;
    private final NoiseChannel noise;

    public final PulseVolumeRegister regSQ1_VOL; // $4000
    public final PulseSweepRegister regSQ1_SWEEP; // $4001
    public final TimerLowRegister regSQ1_LO; // $4002
    public final PulseTimerHighRegister regSQ1_HI; // $4003

    public final PulseVolumeRegister regSQ2_VOL; // $4004
    public final PulseSweepRegister regSQ2_SWEEP; // $4005
    public final TimerLowRegister regSQ2_LO; // $4006
    public final PulseTimerHighRegister regSQ2_HI; // $4007

    public final TriangleLinearRegister regTRI_LINEAR; // $4008
    public final ByteRegister regUNUSED1 = new ByteRegister((byte)0); // $4009
    public final TimerLowRegister regTRI_LO; // $400A
    public final TriangleTimerHighRegister regTRI_HI; // $400B

    public final NoiseVolumeRegister regNOISE_VOL; // $400C
    public final ByteRegister regUNUSED2 = new ByteRegister((byte)0); // $400D
    public final NoisePeriodRegister regNOISE_LO; // $400E
    public final NoiseLengthCounterRegister regNOISE_HI; // $400F

    public final ByteRegister regDMC_FREQ = new ByteRegister((byte)0); // $4010
    public final ByteRegister regDMC_RAW = new ByteRegister((byte)0); // $4011
    public final ByteRegister regDMC_START = new ByteRegister((byte)0); // $4012
    public final ByteRegister regDMC_LEN = new ByteRegister((byte)0); // $4013

    public final StatusRegister regAPUSTATUS; // $4015

    public APU() {
        pulse1 = new PulseChannel();
        pulse2 = new PulseChannel();
        triangle = new TriangleChannel();
        noise = new NoiseChannel();

        regSQ1_VOL = new PulseVolumeRegister(pulse1, this);
        regSQ1_SWEEP = new PulseSweepRegister(pulse1.getSweep(), this);
        regSQ1_LO = new TimerLowRegister(pulse1, this);
        regSQ1_HI = new PulseTimerHighRegister(pulse1, this);

        regSQ2_VOL = new PulseVolumeRegister(pulse2, this);
        regSQ2_SWEEP = new PulseSweepRegister(pulse2.getSweep(), this);
        regSQ2_LO = new TimerLowRegister(pulse2, this);
        regSQ2_HI = new PulseTimerHighRegister(pulse2, this);

        regTRI_LINEAR = new TriangleLinearRegister(triangle, this);
        regTRI_LO = new TimerLowRegister(triangle, this);
        regTRI_HI = new TriangleTimerHighRegister(triangle, this);

        regNOISE_VOL = new NoiseVolumeRegister(noise);
        regNOISE_LO = new NoisePeriodRegister(noise);
        regNOISE_HI = new NoiseLengthCounterRegister(noise);

        regAPUSTATUS = new StatusRegister(pulse1, pulse2, triangle, noise,this);
    }

    private static final int SAMPLE_RATE = 22050;

    public void reset() throws LineUnavailableException {
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
            triangle.clockTimer();
            noise.clockTimer();
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
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    break;
                case 14913:
                    // 2 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    pulse1.clockLengthCounter();
                    pulse2.clockLengthCounter();
                    triangle.clockLengthCounter();
                    noise.clockLengthCounter();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
                case 22371:
                    // 3 envelope & triangles's linear
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    break;
                case 29828:
                    // frame interrupt
                    break;
                case 29829:
                    // 4 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    pulse1.clockLengthCounter();
                    pulse2.clockLengthCounter();
                    triangle.clockLengthCounter();
                    noise.clockLengthCounter();
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
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    break;
                case 14913:
                    // 2 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    pulse1.clockLengthCounter();
                    pulse2.clockLengthCounter();
                    triangle.clockLengthCounter();
                    noise.clockLengthCounter();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
                case 22371:
                    // 3 envelope & triangles's linear
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    break;
                case 37281:
                    // 4 envelope & triangles's linear, length counter & sweep
                    pulse1.clockEnvelope();
                    pulse2.clockEnvelope();
                    noise.clockEnvelope();
                    triangle.clockLinearCounter();
                    pulse1.clockLengthCounter();
                    pulse2.clockLengthCounter();
                    triangle.clockLengthCounter();
                    noise.clockLengthCounter();
                    pulse1.clockSweep();
                    pulse2.clockSweep();
                    break;
            }
        }

        if (cycle % (getBaseFrequency() / SAMPLE_RATE) == 0) {
            // mix pulse1 & pulse2
            buffer[sample % BUFFER_LENGTH] = (byte)(mixPulse()+ mixTriangleNoiseDMC());
//            buffer[sample % BUFFER_LENGTH] = (byte)(mixTriangleNoiseDMC());
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
        int pulse = pulse1.getSignal() + pulse2.getSignal();
        if (pulse == 0) {
            return 0;
        }
        double outputLevel = 95.88 / ((8128.0 / pulse) + 100.0); // 0.0-1.0
        return (byte)(255 * outputLevel);
    }

    private byte mixTriangleNoiseDMC() {
        int tnd = triangle.getSignal() + noise.getSignal();
        if (tnd == 0) {
            return 0;
        }
        double level = 159.79 / ( 1.0 / (triangle.getSignal() / 8227.0 + noise.getSignal() / 12241.0) + 100.0 );
        return (byte)(255 * level);
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
