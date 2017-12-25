package nes.cpu;

import common.ByteRegisterImpl;
import nes.ppu.PPU;

public class OAMDMARegister extends ByteRegisterImpl {

    private final CPU cpu;
    private final PPU ppu;

    public OAMDMARegister(CPU cpu, PPU ppu) {
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
