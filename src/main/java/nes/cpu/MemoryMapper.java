package nes.cpu;

import common.MemoryByte;
import lombok.RequiredArgsConstructor;
import nes.ppu.PPU;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import static nes.cpu.MemoryMapper.Permission.*;

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
        Pair<MemoryByte, Permission> ppuRegister = getPpuRegister(address);
        if (ppuRegister != null) {
            if (ppuRegister.getTwo() == WO) {
                throw new IllegalArgumentException("not readable register");
            }
            return ppuRegister.getOne().get();
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
        Pair<MemoryByte, Permission> ppuRegister = getPpuRegister(address);
        if (ppuRegister != null) {
            if (ppuRegister.getTwo() == RO) {
                throw new IllegalArgumentException("not writable register");
            }
            ppuRegister.getOne().set(value);
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

    enum Permission {RO, WO, RW}

    private Pair<MemoryByte, Permission> getPpuRegister(int address) {
        switch (address) {
            case 0x2000:
                return Tuples.pair(ppu.regPPUCTRL, WO);
            case 0x2001:
                return Tuples.pair(ppu.regPPUMASK, WO);
            case 0x2002:
                return Tuples.pair(ppu.regPPUSTATUS, RO);
            case 0x2003:
                return Tuples.pair(ppu.regOAMADDR, WO);
            case 0x2004:
                return Tuples.pair(ppu.regOAMDATA, RW);
            case 0x2005:
                return Tuples.pair(ppu.regPPUSCROLL, WO);
            case 0x2006:
                return Tuples.pair(ppu.regPPUADDR, WO);
            case 0x2007:
                return Tuples.pair(ppu.regPPUDATA, RW);
            case 0x4014:
                return Tuples.pair(cpu.regOAMDMA, WO);
            case 0x4016:
                return Tuples.pair(cpu.regJOY1, RW);
            case 0x4017:
                return Tuples.pair(cpu.regJOY2, RW);
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
