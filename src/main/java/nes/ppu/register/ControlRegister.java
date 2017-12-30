package nes.ppu.register;

import common.ByteRegisterImpl;

/**
 * https://wiki.nesdev.com/w/index.php/PPU_registers#PPUCTRL

 7  bit  0
 ---- ----
 VPHB SINN
 |||| ||||
 |||| ||++- Base nametable address
 |||| ||    (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
 |||| |+--- VRAM address increment per CPU read/write of PPUDATA
 |||| |     (0: add 1, going across; 1: add 32, going down)
 |||| +---- Sprite pattern table address for 8x8 sprites
 ||||       (0: $0000; 1: $1000; ignored in 8x16 mode)
 |||+------ Background pattern table address (0: $0000; 1: $1000)
 ||+------- Sprite size (0: 8x8; 1: 8x16)
 |+-------- PPU master/slave select
 |          (0: read backdrop from EXT pins; 1: output color on EXT pins)
 +--------- Generate an NMI at the start of the
 vertical blanking interval (0: off; 1: on)
 */
public class ControlRegister extends ByteRegisterImpl {
    public ControlRegister() {
        super((byte)0);
    }

    int addressIncrement() { // bit 2
        return getBit(2) ? 32 : 1;
    }

    public int getBackgroundPatternTable() {
        return getBit(4) ? 1 : 0;
    }

    public int getSpritePatternTable() { return getBit(3) ? 1 : 0; }

    public enum SpriteSize { EIGHT_BY_EIGHT, EIGHT_BY_SIXTEEN };

    public SpriteSize getSpriteSize() {
        return getBit(5) ? SpriteSize.EIGHT_BY_SIXTEEN : SpriteSize.EIGHT_BY_EIGHT;
    }
}
