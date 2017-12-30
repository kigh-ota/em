package nes.apu;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PulseChannel extends ChannelWithEnvelope {

    private int sequencerPhase; // 0-7, proceeds downward

    @Setter
    private int duty; // 0-3

    @Getter
    private final Sweep sweep;

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
        super();
        sweep = new Sweep(this);
    }

    @Override
    void reset() {
        super.reset();
        sequencerPhase = 0;
        duty = 0;

        sweep.reset();
    }

    @Override
    protected int getSignalInternal() {
        return DUTY_TO_WAVEFORM.get(duty)[sequencerPhase] ? getEnvelopedVolume() : 0;
    }

    @Override
    protected boolean isMuted() {
        return super.isMuted() || timerPeriod < 8 || sweep.isMuting();
    }

    void resetSequencerPhase() {
        sequencerPhase = 0;
    }

    @Override
    protected void clockSequencer() {
        if (sequencerPhase == 0) {
            sequencerPhase = 7;
        } else {
            sequencerPhase--;
        }
    }

    // Sweep
    void clockSweep() {
        sweep.clock();
    }

}
