package nes.apu;

public class LengthCounter {

    private static final int[] LENGTH_TABLE = {
            10,254,
            20,  2,
            40,  4,
            80,  6,
            160, 8,
            60, 10,
            14, 12,
            26, 14,
            12, 16,
            24, 18,
            48, 20,
            96, 22,
            192,24,
            72, 26,
            16, 28,
            32, 30
    };

    private final ChannelWithLengthCounter channel;

    private int value;

    LengthCounter(ChannelWithLengthCounter channel) {
        this.channel = channel;
    }

    void reset() {
        value = 0;
    }

    void clock() {
        if (!channel.isEnabled()) {
            value = 0;
            return;
        }
        if (value == 0 || channel.isLengthCounterHalt()) {
            return;
        }
        value--;
    }

    boolean isMuting() {
        return value == 0;
    }

    void setValue(int key) {
        if (!channel.isEnabled()) {
            return;
        }
        this.value = LENGTH_TABLE[key];
    }
}
