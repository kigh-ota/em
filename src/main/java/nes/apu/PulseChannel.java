package nes.apu;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PulseChannel extends Channel {

    private boolean on; // sequencer
    private int sequencerPhase; // 0-7, proceeds downward
    private boolean useConstantVolume; // constant volume or envelope
    @Getter private int volume; // 0-15, also used as the envelope divider period
    private int duty; // 0-3

    // Gates
    @Getter private final Envelope envelope;
    @Getter private final Sweep sweep;

    private static final Map<Integer, Boolean[]> DUTY_TO_WAVEFORM;

    static {
        ImmutableMap.Builder<Integer, Boolean[]> builder = ImmutableMap.builder();
        builder.put(0, new Boolean[]{false, false, false, false, false, false, false, true});
        builder.put(1, new Boolean[]{false, false, false, false, false, false, true, true});
        builder.put(2, new Boolean[]{false, false, false, false, true, true, true, true});
        builder.put(3, new Boolean[]{true, true, true, true, true, true, false, false});
        DUTY_TO_WAVEFORM = builder.build();
    }

    PulseChannel() {
        envelope = new Envelope(this);
        sweep = new Sweep(this);
    }

    @Override
    void reset() {
        on = false;
        sequencerPhase = 0;
        volume = 0;
        timer = 0;
        timerReset = 0;
        duty = 0;

        envelope.reset();
        sweep.reset();
        lengthCounter.reset();
    }

    @Override
    int get() {
        if (!on) {
            return 0;
        }
        return useConstantVolume ? volume : envelope.getDecayLevel();
    }

    void resetSequencerPhase() {
        sequencerPhase = 0;
    }

    @Override
    protected void clockSequencer() {
        if (!enabled || timerReset < 8 || sweep.isMuting() || lengthCounter.isMuting()) {
            on = false;
        } else {
            on = DUTY_TO_WAVEFORM.get(duty)[sequencerPhase];
        }
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

    // Length Counter
    void clockLengthCounter() {
        lengthCounter.clock();
    }
}
