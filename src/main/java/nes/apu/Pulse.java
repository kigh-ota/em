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
    @Getter private int volume; // 0-15, also used as the envelope divider period
    private int timer; // 11 bit, reset when HI written
    @Getter @Setter private int timerReset;
    private int duty; // 0-3
    private int lengthCounter; // ?
    @Getter private boolean lengthCounterHalt; // also used as the envelop loop flag?

    @Getter private final Envelope envelope;
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
        envelope = new Envelope(this);
        sweep = new Sweep(this);
    }

    void reset() {
        on = false;
        sequencerPhase = 0;
        volume = 0;
        timer = 0;
        timerReset = 0;
        duty = 0;

        envelope.reset();
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
        return useConstantVolume ? volume : envelope.getDecayLevel();
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

    public void setLengthCounterHalt(boolean flag) {
        this.lengthCounterHalt = flag;
    }

    public void setUseConstantVolume(boolean useConstantVolume) {
        this.useConstantVolume = useConstantVolume;
    }

    // Envelope
    void clockEnvelope() {
        envelope.clock();
    }

    // Sweep

    void clockSweep() {
        sweep.clock();
    }
}
