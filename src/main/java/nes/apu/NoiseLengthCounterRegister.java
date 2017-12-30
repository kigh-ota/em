package nes.apu;

import common.ByteRegister;

public class NoiseLengthCounterRegister extends ByteRegister {

    private final NoiseChannel noise;

    public NoiseLengthCounterRegister(NoiseChannel noise) {
        super((byte)0);
        this.noise = noise;
    }

    @Override
    public void set(byte value) {
        noise.getLengthCounter().setValue((Byte.toUnsignedInt(value) & 0b11111000) >> 3);
        noise.getEnvelope().setStartFlag(true);
    }
}
