package nes.ppu.register;

import common.ByteRegisterImpl;

/**
 7  bit  0
 ---- ----
 BGRs bMmG
 |||| ||||
 |||| |||+- Greyscale (0: normal color, 1: produce a greyscale display)
 |||| ||+-- 1: Show background in leftmost 8 pixels of screen, 0: Hide
 |||| |+--- 1: Show sprites in leftmost 8 pixels of screen, 0: Hide
 |||| +---- 1: Show background
 |||+------ 1: Show sprites
 ||+------- Emphasize red*
 |+-------- Emphasize green*
 +--------- Emphasize blue*
 */
public class MaskRegister extends ByteRegisterImpl {
    public MaskRegister(byte value) {
        super(value);
    }

    public boolean enableBackground() {
        return getBit(3);
    }

    public boolean enableSprites() {
        return getBit(4);
    }
}
