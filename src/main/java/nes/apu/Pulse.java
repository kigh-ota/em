package nes.apu;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Pulse {

    @Setter private boolean enabled;
    private boolean on; // sequencer
    private int sequencerPhase; // 0-7, proceeds downward
    private boolean useConstantVolume; // constant volume or envelope
    private int volume; // 0-15, also used as the envelope divider period
    private int timer; // 11 bit, reset when HI written
    @Getter @Setter private int timerReset;
    private int duty; // 0-3
    private int lengthCounter; // ?
    private boolean lengthCounterHaltFlag; // also used as the envelop loop flag?

    @Getter private final Sweep sweep;

    private static final Map<Integer, Boolean[]> dutyToWaveform;

    static {
        ImmutableMap.Builder<Integer, Boolean[]> builder = ImmutableMap.builder();
        builder.put(0, new Boolean[]{false, false, false, false, false, false, false, true});
        builder.put(1, new Boolean[]{false, false, false, false, false, false, true, true});
        builder.put(2, new Boolean[]{false, false, false, false, true, true, true, true});
        builder.put(3, new Boolean[]{true, true, true, true, true, true, false, false});
        dutyToWaveform = builder.build();
    }

    Pulse() {
        sweep = new Sweep(this);
    }

    void reset() {
        on = false;
        sequencerPhase = 0;
        volume = 0;
        timer = 0;
        timerReset = 0;
        duty = 0;

        startFlag = false;
        decayLevel = 0;

        sweep.reset();
    }

    /**
     *
     * @return 0-15
     */
    int get() {
        if (!on) {
            return 0;
        }
        return useConstantVolume ? volume : decayLevel;
    }

    /**
     *
     * @param value 0-255
     */
    void setTimerLow(int value) {
        timerReset = (timerReset & 0b11100000000) | value;
    }

    /**
     *
     * @param value 0-7
     */
    void setTimerHigh(int value) {
        timerReset = (timerReset & 0b00011111111) | (value << 8);
    }


    void resetSequencerPhase() {
        sequencerPhase = 0;
    }

    void clockTimer() {
        if (timer == 0) {
            timer = timerReset;
            clockSequencer();
        } else {
            timer--;
        }
    }

    private void clockSequencer() {
        on = (timerReset < 8 || !enabled) ? false : dutyToWaveform.get(duty)[sequencerPhase];
        if (sequencerPhase == 0) {
            sequencerPhase = 7;
        } else {
            sequencerPhase--;
        }
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setDuty(int duty) {
        this.duty = duty;
    }

    public void setLengthCounterHaltFlag(boolean flag) {
        this.lengthCounterHaltFlag = flag;
    }

    public void setUseConstantVolume(boolean useConstantVolume) {
        this.useConstantVolume = useConstantVolume;
    }

    // Envelope

    @Setter private boolean startFlag;
    private int decayLevel;
    private int divider;

    void clockEnvelope() {
        if (startFlag) {
            startFlag = false;
            decayLevel = 15;
        } else {
            clockDivider();
        }
    }

    private void clockDivider() {
        if (divider == 0) {
            divider = volume;
            clockDecayLevelCounter();
        } else {
            divider--;
        }
    }

    private void clockDecayLevelCounter() {
        if (decayLevel == 0) {
            if (lengthCounterHaltFlag) {
                decayLevel = 15;
            }
        } else {
            decayLevel--;
        }
    }

    // Sweep

    void clockSweep() {
        sweep.clock();
    }
}
