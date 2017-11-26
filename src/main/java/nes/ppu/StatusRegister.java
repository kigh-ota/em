package nes.ppu;

import common.ByteRegister;

public class StatusRegister extends ByteRegister {
    final private PPU ppu;

    StatusRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
    }

    @Override
    public byte get() {
        ppu.regPPUADDR.resetLatch();
        ppu.regPPUSCROLL.resetLatch();
        return super.get();
    }

    public void setVblankBit(boolean flag) {
        setBit(flag, 7);
    }
}
