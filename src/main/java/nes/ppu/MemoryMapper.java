package nes.ppu;

import common.BinaryUtil;
import common.ByteArrayMemory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import static nes.ppu.PPU.NAMETABLE_MEMORY_SIZE;
import static nes.ppu.PPU.PALETTE_RAM_SIZE;

@Slf4j
class MemoryMapper {
    private final PPU ppu;

    private static final int NAMETABLE_OFFSET = 0x2000;
    static final int PALETTE_RAM_OFFSET = 0x3F00;
    private static final int SIZE = 0x4000;

    /**
     * $0000-$0FFF Pattern table 0 [left] (256 tiles, 16 bytes each)
     * $1000-$1FFF Pattern table 1 [right]
     * $2000-$23FF Nametable 0
     *   $2000-$23BF Nametable
     *   $23C0-$23FF Attribute table (coloring)
     * $2400-$27FF Nametable 1
     * $2800-$2BFF Nametable 2 (mirror of Nametable 0)
     * $2C00-$2FFF Nametable 3 (mirror of Nametable 1)
     * $3000-$3EFF Mirrors of $2000-$2EFF
     * $3F00-$3F1F Palette RAM indices
     * $3F20-$3FFF Mirrors of $3F00-$3F1F
     */
    MemoryMapper(PPU ppu) {
        this.ppu = ppu;
    }

    public static String getType(int address) {
        if (address < 0) {
            return "N/A";
        } else if (address < 0x1000) {
            return "Pattern Table 0";
        } else if (address < 0x2000) {
            return "Pattern Table 1";
        } else if (address < 0x3F00) {
            int i = ((address - 0x2000) / 0x400) % 2;
            if ((address - 0x2000) % 0x400 < 0x3C0) {
                return String.format("Nametable %d", i);
            } else {
                return String.format("Attribute table %d", i);
            }
        } else if (address < 0x4000) {
            return "Palette RAM";
        } else {
            return "N/A";
        }
    }

    byte get(int address) {
        log.debug("Get: PPU {} (type={})", BinaryUtil.toHexString(address), getType(address));
        Pair<ByteArrayMemory, Integer> memoryOffsetPair = getMemory(address);
        ByteArrayMemory memory = memoryOffsetPair.getOne();
        int offset = memoryOffsetPair.getTwo();
        return memory.get(offset);
    }

    void set(byte value, int address) {
        log.debug("Set PPU {}={} (type={})", BinaryUtil.toHexString(address), BinaryUtil.toHexString(value), getType(address));
        Pair<ByteArrayMemory, Integer> memoryOffsetPair = getMemory(address);
        ByteArrayMemory memory = memoryOffsetPair.getOne();
        int offset = memoryOffsetPair.getTwo();
        if (memory == ppu.characterRom) {
          throw new IllegalArgumentException("Cannot write to pattern tables");
        }
        memory.set(value, offset);
    }

    /**
     * @param address
     * @return (memory, offset)
     */
    private Pair<ByteArrayMemory, Integer> getMemory(int address) {
        if (address < NAMETABLE_OFFSET) {
            return Tuples.pair(ppu.characterRom, address);
        } else if (address < PALETTE_RAM_OFFSET) {
            return Tuples.pair(ppu.nametables, (address - NAMETABLE_OFFSET) % NAMETABLE_MEMORY_SIZE);
        } else if (address < SIZE) {
            return Tuples.pair(ppu.paletteRam, (address - PALETTE_RAM_OFFSET) % PALETTE_RAM_SIZE);
        }
        throw new IllegalArgumentException();
    }
}
