package nes.apu;

import lombok.Getter;
import lombok.Setter;

public abstract class Channel {

    Channel() {
        lengthCounter = new LengthCounter(this);
    }

    @Getter
    protected LengthCounter lengthCounter;

    @Getter
    protected boolean lengthCounterHalt; // also used as the envelop loop flag

    @Setter
    @Getter
    protected boolean enabled;

    protected int timer; // 11 bit, reset when HI written

    @Getter
    @Setter
    protected int timerReset;

    void clockTimer() {
        if (timer == 0) {
            timer = timerReset;
            clockSequencer();
        } else {
            timer--;
        }
    }

    abstract void reset();

    /**
     *
     * @return 0-15
     */
    abstract int get();

    abstract protected void clockSequencer();


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
}
