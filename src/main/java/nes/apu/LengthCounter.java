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

    private final Pulse pulse;

    private int value;

    LengthCounter(Pulse pulse) {
        this.pulse = pulse;
    }

    void reset() {
        value = 0;
    }

    void clock() {
        if (!pulse.isEnabled()) {
            value = 0;
            return;
        }
        if (value == 0 || pulse.isLengthCounterHalt()) {
            return;
        }
        value--;
    }

    boolean isMuting() {
        return value == 0;
    }

    void setValue(int key) {
        if (!pulse.isEnabled()) {
            return;
        }
        this.value = LENGTH_TABLE[key];
    }
}
