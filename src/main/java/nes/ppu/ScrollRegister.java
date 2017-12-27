package nes.ppu;

import common.ByteRegister;
import lombok.Getter;

public class ScrollRegister extends ByteRegister {

    final private PPU ppu;

    @Getter private int x;
    @Getter private int y;

    ScrollRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        x = 0;
        y = 0;
    }

    @Override
    public void set(byte value) {
        if (!ppu.addressLatch) {
            x = Byte.toUnsignedInt(value);
        } else {
            y = Byte.toUnsignedInt(value);
        }
        ppu.addressLatch = !ppu.addressLatch;
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
