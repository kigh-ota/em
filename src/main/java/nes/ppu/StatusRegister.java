package nes.ppu;

import common.ByteRegister;

public class StatusRegister extends ByteRegister {
    final private PPU ppu;

    StatusRegister(PPU ppu) {
        super((byte)0b10000000 /* FIXME */);
        this.ppu = ppu;
    }

    @Override
    public byte get() {
        ppu.regPPUADDR.resetLatch();
        ppu.regPPUSCROLL.resetLatch();
        return super.get();
    }
}
