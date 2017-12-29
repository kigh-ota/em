package nes.apu;

import common.BinaryUtil;

public class StatusRegister extends APURegister {

    PulseChannel pulse1;
    PulseChannel pulse2;

    public StatusRegister(PulseChannel pulse1, PulseChannel pulse2, APU apu) {
        super(apu);
        this.pulse1 = pulse1;
        this.pulse2 = pulse2;
    }

    @Override
    public void set(byte value) {
        super.set(value);
        pulse1.setEnabled(BinaryUtil.getBit(value, 0));
        pulse2.setEnabled(BinaryUtil.getBit(value, 1));
    }

    // TODO implement get()
}
