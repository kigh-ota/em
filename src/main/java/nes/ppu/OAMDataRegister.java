package nes.ppu;

import common.ByteRegister;
import common.MemoryByte;

public class OAMDataRegister extends ByteRegister {

    private final ObjectAttributeMemory oam;
    private final MemoryByte regOAMADDR;

    public OAMDataRegister(ObjectAttributeMemory oam, MemoryByte regOAMADDR) {
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
