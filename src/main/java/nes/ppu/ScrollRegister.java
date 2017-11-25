package nes.ppu;

import common.ByteRegister;
import lombok.Getter;

public class ScrollRegister extends ByteRegister {
    enum Latch {X, Y} // TODO clear latch by reading StatusRegister

    final private PPU ppu;

    private Latch next;

    @Getter private int x;
    @Getter private int y;

    public ScrollRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        next = Latch.X;
        x = 0;
        y = 0;
    }

    @Override
    public void set(byte value) {
        if (next == Latch.X) {
            x = Byte.toUnsignedInt(value);
            next = Latch.Y;
        } else {
            y = Byte.toUnsignedInt(value);
            next = Latch.X;
        }
    }
}
