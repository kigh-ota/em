package nes.apu.channel;

import lombok.Setter;

public class LinearCounter {

    private final TriangleChannel triangle;

    private int value;

    @Setter
    private int load; // 7 bit

    @Setter
    private boolean reloadFlag;

    LinearCounter(TriangleChannel triangle) {
        this.triangle = triangle;
    }

    void reset() {
        value = 0;
        reloadFlag = false;
    }

    void clock() {
        if (reloadFlag) {
            value = load;
        } else if (value != 0) {
            value--;
        }
        if (!triangle.isLengthCounterHalt()) {
            reloadFlag = false;
        }
    }

    boolean isMuting() {
        return value == 0;
    }
}
