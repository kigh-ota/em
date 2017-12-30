package nes.apu;

import common.ByteRegister;
import lombok.extern.slf4j.Slf4j;
import nes.apu.channel.NoiseChannel;
import nes.apu.channel.PulseChannel;
import nes.apu.channel.TriangleChannel;
import nes.apu.register.*;
import nes.cpu.CPU;

import javax.sound.sampled.*;

@Slf4j
public class APU {

    private SourceDataLine line;

    // Registers
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

    private final FrameCounter frameCounter;

    public APU() {
        pulse1 = new PulseChannel();
        pulse2 = new PulseChannel();
        triangle = new TriangleChannel();
        noise = new NoiseChannel();

        regSQ1_VOL = new PulseVolumeRegister(pulse1);
        regSQ1_SWEEP = new PulseSweepRegister(pulse1.getSweep());
        regSQ1_LO = new TimerLowRegister(pulse1);
        regSQ1_HI = new PulseTimerHighRegister(pulse1);

        regSQ2_VOL = new PulseVolumeRegister(pulse2);
        regSQ2_SWEEP = new PulseSweepRegister(pulse2.getSweep());
        regSQ2_LO = new TimerLowRegister(pulse2);
        regSQ2_HI = new PulseTimerHighRegister(pulse2);

        regTRI_LINEAR = new TriangleLinearRegister(triangle);
        regTRI_LO = new TimerLowRegister(triangle);
        regTRI_HI = new TriangleTimerHighRegister(triangle);

        regNOISE_VOL = new NoiseVolumeRegister(noise);
        regNOISE_LO = new NoisePeriodRegister(noise);
        regNOISE_HI = new NoiseLengthCounterRegister(noise);

        regAPUSTATUS = new StatusRegister(pulse1, pulse2, triangle, noise);

        frameCounter = new FrameCounter();
    }

    public void setCpu(CPU cpu) {
        this.frameCounter.setCpu(cpu);
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
        triangle.reset();
        noise.reset();

        frameCounter.reset();

        cycle = 0;
        sample = 0;
        freqParam = 0.7;
    }

    private static final int BASE_FREQ = 1789773; // Hz
    private static final int BUFFER_LENGTH = 110;

    private double freqParam;

    private byte[] buffer = new byte[BUFFER_LENGTH];

    private long cycle;
    private int sample;

    public void runStep() {
        runStepInner();
        cycle++;
    }

    private void runStepInner() {

        // Frequency
        if (cycle % 2 == 0) {
            pulse1.clockTimer();
            pulse2.clockTimer();
            triangle.clockTimer();
            noise.clockTimer();
        }

        // Envelope, Length Counters, Sweep
        frameCounter.clock();
        if (frameCounter.isQuarterFrame()) {
            pulse1.clockEnvelope();
            pulse2.clockEnvelope();
            noise.clockEnvelope();
            triangle.clockLinearCounter();
        }
        if (frameCounter.isHalfFrame()) {
            pulse1.clockLengthCounter();
            pulse2.clockLengthCounter();
            triangle.clockLengthCounter();
            noise.clockLengthCounter();
            pulse1.clockSweep();
            pulse2.clockSweep();
        }

        // Sampling, Mixing, Output
        if (cycle % (getBaseFrequency() / SAMPLE_RATE) == 0) {
            // mix pulse1 & pulse2
            buffer[sample % BUFFER_LENGTH] = (byte)(mixPulse()+ mixTriangleNoiseDMC());
//            buffer[sample % BUFFER_LENGTH] = (byte)(mixTriangleNoiseDMC());
//            log.warn("{}", buffer[sample % BUFFER_LENGTH]);
            sample++;
            if (sample % BUFFER_LENGTH == 0) {
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

    // TODO frame interrupt

}
