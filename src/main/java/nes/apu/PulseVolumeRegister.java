package nes.apu;

import common.BinaryUtil;

// Duty, Length counter halt, Volume/Envelope flag, Volume
public class PulseVolumeRegister extends APURegister {

    private final Pulse pulse;

    public PulseVolumeRegister(Pulse pulse, APU apu) {
        super(apu);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setDuty((Byte.toUnsignedInt(value) & 0b11000000) >> 6);
        pulse.setLengthCounterHalt(BinaryUtil.getBit(value, 6));
        pulse.setUseConstantVolume(BinaryUtil.getBit(value, 5));
        pulse.setVolume(Byte.toUnsignedInt(value) & 0b00001111);
    }
}
