package nes.apu.register;

import common.ByteRegister;
import nes.apu.channel.NoiseChannel;

public class NoiseLengthCounterRegister implements ByteRegister {

    private final NoiseChannel noise;

    public NoiseLengthCounterRegister(NoiseChannel noise) {
        this.noise = noise;
    }

    @Override
    public void set(byte value) {
        noise.getLengthCounter().setValue((Byte.toUnsignedInt(value) & 0b11111000) >> 3);
        noise.getEnvelope().setStartFlag(true);
    }
}
