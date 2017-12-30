package nes.ppu.register;

import common.ByteRegister;
import nes.ppu.ObjectAttributeMemory;

public class OAMDataRegister implements ByteRegister {

    private final ObjectAttributeMemory oam;
    private final ByteRegister regOAMADDR;

    public OAMDataRegister(ObjectAttributeMemory oam, ByteRegister regOAMADDR) {
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
