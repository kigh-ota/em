package nes.ppu;

import common.MemoryByte;

import static com.google.common.base.Preconditions.checkArgument;

public class MemoryMapper {
    private final PPU ppu;

    public static final int NAMETABLE_OFFSET = 0x2000;
    public static final int NAMETABLE_RIGHT = 0x3000;
    public static final int PALETTE_RAM_OFFSET = 0x3F00;
    public static final int PALETTE_RAM_RIGHT = 0x3F20;

    /**
     * $0000-$0FFF Pattern table 0 [left] (256 tiles, 16 bytes each)
     * $1000-$1FFF Pattern table 1 [right]
     * $2000-$23FF Nametable 0
     *   $2000-$23BF Nametable
     *   $23C0-$23FF Attribute table (coloring)
     * $2400-$27FF Nametable 1
     * $2800-$2BFF Nametable 2
     * $2C00-$2FFF Nametable 3
     * $3000-$3EFF Mirrors of $2000-$2EFF
     * $3F00-$3F1F Palette RAM indices
     * $3F20-$3FFF Mirrors of $3F00-$3F1F
     */
    MemoryMapper(PPU ppu) {
        this.ppu = ppu;
    }

    byte get(int address) {
        return at(address).get();
    }

    void set(byte value, int address) {
        checkWritable(address);
        at(address).set(value);
    }

    private void checkWritable(int address) {
        checkArgument(address >= NAMETABLE_OFFSET && address < PALETTE_RAM_RIGHT);
    }

    private MemoryByte at(int address) {
        checkArgument(address >= 0);
        if (address < NAMETABLE_OFFSET) {
            // pattern tables
            return ppu.characterRom.at(address);
        } else if (address < NAMETABLE_RIGHT) {
            return ppu.nametables.at(address - NAMETABLE_OFFSET);
        } else if (address >= PALETTE_RAM_OFFSET && address < PALETTE_RAM_RIGHT) {
            return ppu.paletteRam.at(address - PALETTE_RAM_OFFSET);
        }
        throw new IllegalArgumentException();
    }
}
