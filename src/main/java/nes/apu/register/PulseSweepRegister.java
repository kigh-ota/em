package nes.apu.register;

import common.BinaryUtil;
import common.ByteRegister;
import nes.apu.channel.Sweep;

// Sweep
public class PulseSweepRegister extends ByteRegister {

    private final Sweep sweep;

    public PulseSweepRegister(Sweep sweep) {
        super((byte)0);
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
