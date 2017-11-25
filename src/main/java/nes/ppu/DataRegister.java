package nes.ppu;

import common.ByteRegister;

public class DataRegister extends ByteRegister {
    private final PPU ppu;

    public DataRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
    }

    @Override
    public void set(byte value) {
        int address = ppu.regPPUADDR.getAddress();
        ppu.getMemoryMapper().set(value, address);
        ppu.regPPUADDR.incrementAddress();
    }

    @Override
    public byte get() {
        int address = ppu.regPPUADDR.getAddress();
        byte value = ppu.getMemoryMapper().get(address);
        ppu.regPPUADDR.incrementAddress();
        return value;
    }
}
