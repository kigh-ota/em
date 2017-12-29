package nes.apu;

import common.BinaryUtil;

// Sweep
public class PulseSweepRegister extends APURegister {

    private final Sweep sweep;

    PulseSweepRegister(Sweep sweep, APU apu) {
        super(apu);
        this.sweep = sweep;
    }

    @Override
    public void set(byte value) {
        sweep.setEnabled(BinaryUtil.getBit(value, 7));
        sweep.setDividerReset((Byte.toUnsignedInt(value) & 0b01110000) >> 4);
        sweep.setNegateFlag(BinaryUtil.getBit(value, 3));
        sweep.setShiftCount(Byte.toUnsignedInt(value) & 7);
        sweep.setReloadFlag(true);
    }
}
