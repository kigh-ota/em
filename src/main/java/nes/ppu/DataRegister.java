package nes.ppu;

import common.ByteRegister;

class DataRegister extends ByteRegister {
    private final PPU ppu;

    DataRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
    }

    @Override
    public void set(byte value) {
        int address = ppu.regPPUADDR.getAddress();
        ppu.memoryMapper.set(value, address);
        ppu.regPPUADDR.incrementAddress();
    }

    @Override
    public byte get() {
        int address = ppu.regPPUADDR.getAddress();
        byte value = ppu.memoryMapper.get(address);
        ppu.regPPUADDR.incrementAddress();
        return value;
    }
}
