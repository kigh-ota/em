package nes.ppu.register;

import common.BinaryUtil;
import common.ByteRegister;
import lombok.extern.slf4j.Slf4j;
import nes.ppu.PPU;

import static nes.ppu.MemoryMapper.PALETTE_RAM_OFFSET;

@Slf4j
public class DataRegister extends ByteRegister {
    private final PPU ppu;

    private byte readBuffer;

    public DataRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        readBuffer = 0;
    }

    @Override
    public void set(byte value) {
        int address = ppu.regPPUADDR.getAddress();
        if (log.isDebugEnabled()) {
            log.debug("set PPU: addr={}", BinaryUtil.toHexString(address));
        }
        ppu.memoryMapper.set(value, address);
        ppu.regPPUADDR.incrementAddress();
    }

    @Override
    public byte get() {
        int address = ppu.regPPUADDR.getAddress();
        byte value = ppu.memoryMapper.get(address);
        byte ret = address < PALETTE_RAM_OFFSET ? readBuffer : value;
        readBuffer = value;
        log.debug("get PPU: addr={}, value={}", BinaryUtil.toHexString(address), BinaryUtil.toHexString(value));
        ppu.regPPUADDR.incrementAddress();
        return ret;
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
