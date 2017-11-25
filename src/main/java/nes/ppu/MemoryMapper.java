package nes.ppu;

import common.MemoryByte;

import static com.google.common.base.Preconditions.checkArgument;

class MemoryMapper {
    private final PPU ppu;

    private static final int NAMETABLE_OFFSET = 0x2000;
    private static final int NAMETABLE_RIGHT = 0x2800;
    private static final int PALETTE_RAM_OFFSET = 0x3F00;
    private static final int PALETTE_RAM_RIGHT = 0x4000;

    /**
     * $0000-$0FFF Pattern table 0 [left] (256 tiles, 16 bytes each)
     * $1000-$1FFF Pattern table 1 [right]
     * $2000-$23FF Nametable 0
     *   $2000-$23BF Nametable
     *   $23C0-$23FF Attribute table (coloring)
     * $2400-$27FF Nametable 1
     * ($2800-$2BFF Nametable 2)
     * ($2C00-$2FFF Nametable 3)
     * $3000-$3EFF Mirrors of $2000-$2EFF
     * $3F00-$3F1F Palette RAM indices
     * $3F20-$3FFF Mirrors of $3F00-$3F1F TODO
     */
    MemoryMapper(PPU ppu) {
        this.ppu = ppu;
    }

    byte get(int address) {
        if (address < NAMETABLE_OFFSET) {
            // pattern tables
            return ppu.characterRom.get(address);
        } else if (address < NAMETABLE_RIGHT) {
            return ppu.nametables.get(address - NAMETABLE_OFFSET);
        } else if (address >= PALETTE_RAM_OFFSET && address < PALETTE_RAM_RIGHT) {
            return ppu.paletteRam.get(address - PALETTE_RAM_OFFSET);
        }
        throw new IllegalArgumentException();
    }

    void set(byte value, int address) {
        if (address < NAMETABLE_OFFSET) {
            throw new IllegalArgumentException("Cannot write to pattern tables");
        } else if (address < NAMETABLE_RIGHT) {
            ppu.nametables.set(value, address - NAMETABLE_OFFSET);
        } else if (address >= PALETTE_RAM_OFFSET && address < PALETTE_RAM_RIGHT) {
            ppu.paletteRam.set(value, address - PALETTE_RAM_OFFSET);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
