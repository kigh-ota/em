package nes.apu;

import lombok.Getter;
import lombok.Setter;

public class Envelope {

    private final ChannelWithEnvelope channel;

    @Setter
    private boolean startFlag;

    @Getter
    private int decayLevel;

    private int divider;

    Envelope(ChannelWithEnvelope channel) {
        this.channel = channel;
    }

    void reset() {
        startFlag = false;
        decayLevel = 0;
        divider = 0;
    }

    void clock() {
        if (startFlag) {
            startFlag = false;
            decayLevel = 15;
        } else {
            clockDivider();
        }
    }

    private void clockDivider() {
        if (divider == 0) {
            divider = channel.getVolume();
            clockDecayLevelCounter();
        } else {
            divider--;
        }
    }

    private void clockDecayLevelCounter() {
        if (decayLevel == 0) {
            if (channel.isLengthCounterHalt()) {
                decayLevel = 15;
            }
        } else {
            decayLevel--;
        }
    }

}
