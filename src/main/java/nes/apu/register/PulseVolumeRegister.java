package nes.apu.register;

import common.BinaryUtil;
import common.ByteRegister;
import nes.apu.channel.PulseChannel;

// Duty, Length counter halt, Volume/Envelope flag, Volume
public class PulseVolumeRegister extends ByteRegister {

    private final PulseChannel pulse;

    public PulseVolumeRegister(PulseChannel pulse) {
        super((byte)0);
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
