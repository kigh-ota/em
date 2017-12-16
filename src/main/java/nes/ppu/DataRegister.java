package nes.ppu;

import common.BinaryUtil;
import common.ByteRegister;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DataRegister extends ByteRegister {
    private final PPU ppu;

    DataRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
    }

    @Override
    public void set(byte value) {
        int address = ppu.regPPUADDR.getAddress();
        log.debug("set PPU: addr={}", BinaryUtil.toHexString(address));
        ppu.memoryMapper.set(value, address);
        ppu.regPPUADDR.incrementAddress();
    }

    @Override
    public byte get() {
        int address = ppu.regPPUADDR.getAddress();
        byte value = ppu.memoryMapper.get(address);
        log.debug("get PPU: addr={}, value={}", BinaryUtil.toHexString(address), BinaryUtil.toHexString(value));
        ppu.regPPUADDR.incrementAddress();
        return value;
    }

    @Override
    public boolean increment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean decrement() {
        throw new UnsupportedOperationException();
    }
}
