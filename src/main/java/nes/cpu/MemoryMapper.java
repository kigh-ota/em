package nes.cpu;

import common.MemoryByte;
import lombok.RequiredArgsConstructor;
import nes.ppu.PPU;

@RequiredArgsConstructor
class MemoryMapper {
    /**
     * https://wiki.nesdev.com/w/index.php/CPU_memory_map
     *
     * $0000-$07FF 2KB internal RAM
     * $0800-$0FFF (mirror)
     * $1000-$17FF (mirror)
     * $1800-$1FFF (mirror)
     * $2000-$2007 PPU registers
     * $2008-$3FFF (mirrors)
     * $4000-$4017 APU & I/O registers
     * $4018-$401F ?
     * $4020-$FFFF Cartridge space: PRG ROM, PRG RAM, mapper registers
     *   commonly:
     *     $6000-$7FFF Battery Backed Save/Work RAM
     *     $8000-$FFFF Program ROM
     */
    static final int PROGRAM_OFFSET = 0x8000;

    private final _6502 cpu;
    private final PPU ppu;

    byte get(int address) {
        checkAddress(address);
        MemoryByte ppuRegister = getPpuRegister(address);
        if (ppuRegister != null) {
            return ppuRegister.get();
        }
        if (address < 0x800) {
            return cpu.ram.get(address);
        } else if (address >= PROGRAM_OFFSET && address < 0x10000) {
            return cpu.programRom.get(address - PROGRAM_OFFSET);
        }
        throw new IllegalArgumentException();
    }

    void set(byte value, int address) {
        checkAddress(address);
        MemoryByte ppuRegister = getPpuRegister(address);
        if (ppuRegister != null) {
            ppuRegister.set(value);
            return;
        }

        if (address < 0x800) {
            cpu.ram.set(value, address);
            return;
        } else if (address >= PROGRAM_OFFSET && address < 0x10000) {
            throw new IllegalArgumentException("Cannot write to program ROM");
        } else if (address >= 0x4000 && address < 0x4018) {
            return;
        }
        throw new IllegalArgumentException();
    }

    private MemoryByte getPpuRegister(int address) {
        switch (address) {
            case 0x2000:
                return ppu.regPPUCTRL;
            case 0x2001:
                return ppu.regPPUMASK;
            case 0x2002:
                return ppu.regPPUSTATUS;
            case 0x2003:
                return ppu.regOAMADDR;
            case 0x2004:
                return ppu.regOAMDATA;
            case 0x2005:
                return ppu.regPPUSCROLL;
            case 0x2006:
                return ppu.regPPUADDR;
            case 0x2007:
                return ppu.regPPUDATA;
        }
        return null;
    }

    private static void checkAddress(int address) {
        if (address < 0) {
            throw new IllegalArgumentException("Negative address");
        } else if (address >= 0x2008 && address < 0x4000) {
            throw new IllegalArgumentException("Mirror of PPU registers");
        } else if (address >= 0x4000 && address < 0x4018) {
            System.err.println("access to APU & I/O registers");
//            throw new IllegalArgumentException("APU & I/O registers");
        } else if (address >= 0x4018 && address < PROGRAM_OFFSET) {
            throw new IllegalArgumentException();
        } else if (address >= 0x10000) {
            throw new IllegalArgumentException();
        }
    }
}
