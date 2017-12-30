package nes.apu;

import common.BinaryUtil;
import common.ByteRegister;
import nes.apu.channel.NoiseChannel;

public class NoiseVolumeRegister extends ByteRegister {

    private final NoiseChannel noise;

    public NoiseVolumeRegister(NoiseChannel noise) {
        super((byte)0);
        this.noise = noise;
    }

    @Override
    public void set(byte value) {
        noise.setLengthCounterHalt(BinaryUtil.getBit(value, 5));
        noise.setUseConstantVolume(BinaryUtil.getBit(value, 4));
        noise.setVolume(Byte.toUnsignedInt(value) & 15);
    }
}
