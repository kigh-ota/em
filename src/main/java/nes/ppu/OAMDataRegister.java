package nes.ppu;

import common.ByteRegisterImpl;
import common.ByteRegister;

public class OAMDataRegister extends ByteRegisterImpl {

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
