package nes.ppu;

import common.ByteArrayMemory;

public class PaletteRam extends ByteArrayMemory {

    static final int SIZE = 0x20;

    public PaletteRam() {
        super(new byte[SIZE]);
    }

    @Override
    public void set(byte value, int offset) {
        if (offset % 4 == 0) {
            // mirroring
            super.set(value, offset % 0x10);
            super.set(value, offset % 0x10 + 0x10);
        } else {
            super.set(value, offset);
        }
    }

}
