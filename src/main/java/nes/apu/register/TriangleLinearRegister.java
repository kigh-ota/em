package nes.apu.register;

import common.BinaryUtil;
import common.ByteRegister;
import nes.apu.channel.TriangleChannel;

public class TriangleLinearRegister implements ByteRegister {

    private final TriangleChannel triangle;

    public TriangleLinearRegister(TriangleChannel triangle) {
        this.triangle = triangle;
    }

    @Override
    public void set(byte value) {
        triangle.setLengthCounterHalt(BinaryUtil.getBit(value, 7));
        triangle.getLinearCounter().setLoad(Byte.toUnsignedInt(value) & 0b01111111);
    }
}
