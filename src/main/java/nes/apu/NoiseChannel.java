package nes.apu;

import common.BinaryUtil;
import lombok.Setter;

public class NoiseChannel extends ChannelWithEnvelope {

    NoiseChannel() {
        super();
        shiftRegister = 1;
    }

    @Setter
    private boolean modeFlag;

    private int shiftRegister;

    @Override
    void reset() {
        super.reset();
        modeFlag = false;
    }

    @Override
    protected int getSignalInternal() {
        return BinaryUtil.getBit(shiftRegister, 0) ? 0 : getEnvelopedVolume();
    }

    @Override
    protected boolean isMuted() {
        return super.isMuted();
    }

    @Override
    protected void clockSequencer() {
        // bit 0 ^ bit 6/1
        int feedback = (shiftRegister & 1) ^ ((shiftRegister >> (modeFlag ? 6 : 1)) & 1);
        shiftRegister = (shiftRegister & 0b0111111111111111) | (feedback << 15);
        shiftRegister >>= 1;
    }

    private static final int[] PERIOD = {
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
    };

    /**
     *
     * @param key 0-15
     */
    void setNoisePeriod(int key) {
        timerPeriod = PERIOD[key];
    }
}
