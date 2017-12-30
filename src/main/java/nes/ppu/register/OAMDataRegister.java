package nes.ppu.register;

import common.ByteRegister;
import nes.ppu.ObjectAttributeMemory;

public class OAMDataRegister extends ByteRegister {

    private final ObjectAttributeMemory oam;
    private final ByteRegister regOAMADDR;

    public OAMDataRegister(ObjectAttributeMemory oam, ByteRegister regOAMADDR) {
        super((byte)0);
        this.oam = oam;
        this.regOAMADDR = regOAMADDR;
    }

    @Override
    public void set(byte value) {
        oam.set(value, Byte.toUnsignedInt(regOAMADDR.get()));
        regOAMADDR.increment();
    }

    @Override
    public byte get() {
        return oam.get(Byte.toUnsignedInt(regOAMADDR.get()));
    }
}
