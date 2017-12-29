package nes.apu;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Pulse {

    private boolean enabled;
    private boolean on; // sequencer
    private int sequencerPhase; // 0-7, proceeds downward
    private int volume; // 0-15
    private int timer; // 11 bit, reset when HI written
    private int timerReset;
    private int duty; // 0-3
    private int lengthCounter; // ?
    private boolean lengthCounterHalt; // ?
    private boolean useConstantVolume;

    private static final Map<Integer, Boolean[]> dutyToWaveform;

    static {
        ImmutableMap.Builder<Integer, Boolean[]> builder = ImmutableMap.builder();
        builder.put(0, new Boolean[]{false, false, false, false, false, false, false, true});
        builder.put(1, new Boolean[]{false, false, false, false, false, false, true, true});
        builder.put(2, new Boolean[]{false, false, false, false, true, true, true, true});
        builder.put(3, new Boolean[]{true, true, true, true, true, true, false, false});
        dutyToWaveform = builder.build();
    }

    void reset() {
        on = false;
        sequencerPhase = 0;
        volume = 0;
        timer = 0;
        timerReset = 0;
        duty = 0;
    }

    /**
     *
     * @return 0-15
     */
    int get() {
        return on ? 15 : 0;
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
//            log.warn("enabled {}", this)
        }
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setDuty(int duty) {
        this.duty = duty;
    }

    public void setLengthCounterHalt(boolean lengthCounterHalt) {
        this.lengthCounterHalt = lengthCounterHalt;
    }

    public void setUseConstantVolume(boolean useConstantVolume) {
        this.useConstantVolume = useConstantVolume;
    }
}
