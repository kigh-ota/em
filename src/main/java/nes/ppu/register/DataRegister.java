package nes.ppu.register;

import common.BinaryUtil;
import common.ByteRegister;
import lombok.extern.slf4j.Slf4j;
import nes.ppu.PPU;

import static nes.ppu.MemoryMapper.PALETTE_RAM_OFFSET;

@Slf4j
public class DataRegister implements ByteRegister {
    private final PPU ppu;

    private byte readBuffer;

    public DataRegister(PPU ppu) {
        this.ppu = ppu;
        readBuffer = 0;
    }

    @Override
    public void set(byte value) {
        int address = ppu.regPPUADDR.getAddress();
        if (log.isDebugEnabled()) {
            log.debug("set PPU: addr={}", BinaryUtil.toHexString(address));
        }
        ppu.write(value, address);
        ppu.regPPUADDR.incrementAddress();
    }

    @Override
    public byte get() {
        int address = ppu.regPPUADDR.getAddress();
        byte value = ppu.read(address);
        byte ret = address < PALETTE_RAM_OFFSET ? readBuffer : value;
        readBuffer = value;
        log.debug("get PPU: addr={}, value={}", BinaryUtil.toHexString(address), BinaryUtil.toHexString(value));
        ppu.regPPUADDR.incrementAddress();
        return ret;
    }

}
