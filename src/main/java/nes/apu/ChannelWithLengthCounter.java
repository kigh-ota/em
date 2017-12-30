package nes.apu;

import lombok.Getter;

public abstract class ChannelWithLengthCounter extends Channel {

    @Getter
    protected LengthCounter lengthCounter;

    @Getter
    protected boolean lengthCounterHalt; // also used as the envelop loop flag

    ChannelWithLengthCounter() {
        super();
        lengthCounter = new LengthCounter(this);
    }

    @Override
    void reset() {
        super.reset();
        lengthCounter.reset();
    }

    // Length Counter
    void clockLengthCounter() {
        lengthCounter.clock();
    }

    public void setLengthCounterHalt(boolean flag) {
        this.lengthCounterHalt = flag;
    }

    @Override
    protected boolean isMuted() {
        return super.isMuted() || lengthCounter.isMuting();
    }
}
