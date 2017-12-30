package nes.apu;

import common.BinaryUtil;
import common.ByteRegister;

public class NoisePeriodRegister extends ByteRegister {

    private final NoiseChannel noise;

    public NoisePeriodRegister(NoiseChannel noise) {
        super((byte)0);
        this.noise = noise;
    }

    @Override
    public void set(byte value) {
        noise.setNoisePeriod(Byte.toUnsignedInt(value) & 15);
        noise.setModeFlag(BinaryUtil.getBit(value, 7));
    }
}
