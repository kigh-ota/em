package nes.cpu;

import common.ByteRegister;
import nes.ppu.PPU;

public class OAMDMARegister extends ByteRegister {

    private final _6502 cpu;
    private final PPU ppu;

    public OAMDMARegister(_6502 cpu, PPU ppu) {
        super((byte)0);
        this.cpu = cpu;
        this.ppu = ppu;
    }

    @Override
    public void set(byte value) {
        // copy $XX00-$XXFF to PPU OAM
        final int base = Byte.toUnsignedInt(value) * 0x100;
        for (int offset = 0; offset < 0x100; offset++) {
            final int address = base + offset;
            ppu.oam.set(cpu.memoryMapper.get(address), offset);
        }
    }
}
