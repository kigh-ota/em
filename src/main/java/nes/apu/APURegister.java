package nes.apu;

import common.ByteRegister;

public abstract class APURegister extends ByteRegister {

    protected final APU apu;

    APURegister(APU apu) {
        super((byte)0);
        this.apu = apu;
    }

}
