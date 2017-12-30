package nes.apu;

import common.BinaryUtil;
import nes.apu.channel.TriangleChannel;

public class TriangleLinearRegister extends APURegister {

    private final TriangleChannel triangle;

    TriangleLinearRegister(TriangleChannel triangle, APU apu) {
        super(apu);
        this.triangle = triangle;
    }

    @Override
    public void set(byte value) {
        triangle.setLengthCounterHalt(BinaryUtil.getBit(value, 7));
        triangle.getLinearCounter().setLoad(Byte.toUnsignedInt(value) & 0b01111111);
    }
}
