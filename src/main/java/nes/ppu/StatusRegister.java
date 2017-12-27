package nes.ppu;

import common.ByteRegister;

/**
 * https://wiki.nesdev.com/w/index.php/PPU_registers#Status_.28.242002.29_.3C_read
 *
 * 7  bit  0
 * ---- ----
 * VSO. ....
 * |||| ||||
 * |||+-++++- Least significant bits previously written into a PPU register
 * |||        (due to register not being updated for this address)
 * ||+------- Sprite overflow. The intent was for this flag to be set
 * ||         whenever more than eight sprites appear on a scanline, but a
 * ||         hardware bug causes the actual behavior to be more complicated
 * ||         and generate false positives as well as false negatives; see
 * ||         PPU sprite evaluation. This flag is set during sprite
 * ||         evaluation and cleared at dot 1 (the second dot) of the
 * ||         pre-render line.
 * |+-------- Sprite 0 Hit.  Set when a nonzero pixel of sprite 0 overlaps
 * |          a nonzero background pixel; cleared at dot 1 of the pre-render
 * |          line.  Used for raster timing.
 * +--------- Vertical blank has started (0: not in vblank; 1: in vblank).
 * Set at dot 1 of line 241 (the line *after* the post-render
 * line); cleared after reading $2002 and at dot 1 of the
 * pre-render line.
 */
public class StatusRegister extends ByteRegister {
    final private PPU ppu;

    StatusRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
    }

    @Override
    public byte get() {
        byte value = super.get();
        setVBlank(false);
        ppu.addressLatch = false;
        return value;
    }

    void setVBlank(boolean flag) {
        setBit(flag, 7);
    }

    void setSprite0Hit(boolean flag) { setBit(flag, 6); }

    void setSpriteOverflow(boolean flag) { setBit(flag, 5); }

    @Override
    public boolean increment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean decrement() {
        throw new UnsupportedOperationException();
    }
}
