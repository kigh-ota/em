package nes.apu;

public class TriangleChannel extends Channel {

    private int phase;

    private static final int[] WAVEFORM = {
            15, 14, 13, 12, 11, 10,  9,  8,  7,  6,  5,  4,  3,  2,  1,  0,
             0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15
    };

    @Override
    void reset() {
        phase = 0;
    }

    @Override
    int get() {
        return enabled ? WAVEFORM[phase] : 0;
    }

    @Override
    protected void clockSequencer() {
        if (phase == 0) {
            phase = WAVEFORM.length - 1;
        } else {
            phase--;
        }
    }

}
