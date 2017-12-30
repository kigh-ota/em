package nes.apu.channel;

import lombok.Getter;
import lombok.Setter;

public abstract class Channel {

    Channel() { }

    @Setter
    @Getter
    protected boolean enabled;

    protected int timer; // for adjusting frequency. 11 bit, reset when HI written

    @Getter
    @Setter
    protected int timerPeriod;

    public void clockTimer() {
        if (timer == 0) {
            timer = timerPeriod;
            clockSequencer();
        } else {
            timer--;
        }
    }

    void reset() {
        timer = 0;
        timerPeriod = 0;
        enabled = false;
    }

    /**
     *
     * @return 0-15
     */
    public final int getSignal() {
        if (isMuted()) {
            return 0;
        }
        return getSignalInternal();
    };

    // override to give waveform
    abstract protected int getSignalInternal();

    // override to give mute condition
    protected boolean isMuted() {
        return !enabled;
    }

    abstract protected void clockSequencer();

    /**
     *
     * @param value 0-255
     */
    public void setTimerLow(int value) {
        timerPeriod = (timerPeriod & 0b11100000000) | value;
    }

    /**
     *
     * @param value 0-7
     */
    public void setTimerHigh(int value) {
        timerPeriod = (timerPeriod & 0b00011111111) | (value << 8);
    }

}
