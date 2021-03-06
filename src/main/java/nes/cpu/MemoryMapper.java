package nes.cpu;

import common.ByteArrayMemory;
import common.ByteRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nes.apu.APU;
import nes.ppu.PPU;
import org.eclipse.collections.api.tuple.Pair;

import static nes.cpu.MemoryMapper.Permission.*;
import static nes.cpu.MemoryMapper.Type.MEMORY;
import static nes.cpu.MemoryMapper.Type.REGISTER;
import static org.eclipse.collections.impl.tuple.Tuples.pair;

@RequiredArgsConstructor
@Slf4j
public class MemoryMapper {
    /**
     * https://wiki.nesdev.com/w/index.php/CPU_memory_map
     *
     * $0000-$07FF 2KB internal RAM
     * $0800-$0FFF (mirror)
     * $1000-$17FF (mirror)
     * $1800-$1FFF (mirror)
     * $2000-$2007 PPU registers
     * $2008-$3FFF (mirrors)
     * $4000-$4017 apu & I/O registers
     * $4018-$401F ?
     * $4020-$FFFF Cartridge space: PRG ROM, PRG RAM, mapper registers
     *   commonly:
     *     $6000-$7FFF Battery Backed Save/Work RAM
     *     $8000-$FFFF Program ROM
     */
    static final int PROGRAM_OFFSET = 0x8000;

    enum Type { REGISTER, MEMORY };
    enum Permission {RO, WO, RW}

    Type getType(int address) {
        if (address >= 0x0000 && address < 0x2000) {
            return MEMORY;
        } else if (address >= 0x2000 && address < 0x4000) {
            return REGISTER;
        } else if (address >= 0x4000 && address < 0x4018) {
            log.debug("access to APU & I/O registers");
            return REGISTER;
        } else if (address >= 0x4018 && address < 0x4020) {
            throw new IllegalArgumentException("APU and I/O functionality that is normally disabled");
        } else if (address >= 0x4020 && address < 0x10000) {
            return MEMORY;
        }
        throw new IllegalArgumentException("address out of range");
    }

    private final CPU cpu;
    private final PPU ppu;
    private final APU apu;

    public byte get(int address) {
        switch (getType(address)) {
            case REGISTER:
                ByteRegister register = getReadableRegister(address);
                return register.get();
            case MEMORY:
                Pair<ByteArrayMemory, Integer> pair = getReadableMemory(address);
                return pair.getOne().get(pair.getTwo());
        }
        throw new IllegalArgumentException();
    }

    void set(byte value, int address) {
        switch (getType(address)) {
            case REGISTER:
                ByteRegister register = getWritableRegister(address);
                register.set(value);
                return;
            case MEMORY:
                Pair<ByteArrayMemory, Integer> pair = getWritableMemory(address);
                pair.getOne().set(value, pair.getTwo());
                return;
        }
        throw new IllegalArgumentException();
    }

    byte increment(int address) {
        switch (getType(address)) {
            case REGISTER:
                ByteRegister register = getWritableRegister(address);
                register.increment();
                return register.get();
            case MEMORY:
                Pair<ByteArrayMemory, Integer> pair = getWritableMemory(address);
                return pair.getOne().increment(pair.getTwo());
        }
        throw new IllegalArgumentException();
    }

    byte decrement(int address) {
        switch (getType(address)) {
            case REGISTER:
                ByteRegister register = getWritableRegister(address);
                register.decrement();
                return register.get();
            case MEMORY:
                Pair<ByteArrayMemory, Integer> pair = getWritableMemory(address);
                return pair.getOne().decrement(pair.getTwo());
        }
        throw new IllegalArgumentException();
    }

    /**
     *
     * @param address
     * @return pair of memory and offset
     */
    private Pair<ByteArrayMemory, Integer> getWritableMemory(int address) {
        Pair<Pair<ByteArrayMemory, Integer>, Permission> pair = getMemory(address);
        if (pair.getTwo() == RO) {
            throw new IllegalArgumentException("not writable memory");
        }
        return pair.getOne();
    }

    private Pair<ByteArrayMemory, Integer> getReadableMemory(int address) {
        Pair<Pair<ByteArrayMemory, Integer>, Permission> pair = getMemory(address);
        if (pair.getTwo() == WO) {
            throw new IllegalArgumentException("not readable memory");
        }
        return pair.getOne();
    }

    private Pair<Pair<ByteArrayMemory, Integer>, Permission> getMemory(int address) {
        if (address < 0x2000) {
            return pair(pair(cpu.ram, address % 0x800), RW);
        } else if (address >= 0x4020 && address < PROGRAM_OFFSET) {
            throw new IllegalArgumentException("unimplemented memory");
        } else if (address >= PROGRAM_OFFSET && address < 0x10000) {
            return pair(pair(cpu.programRom, address - PROGRAM_OFFSET), RO);
        }
        throw new IllegalArgumentException();
    }

    private ByteRegister getWritableRegister(int address) {
        Pair<ByteRegister, Permission> pair = getRegister(address);
        if (pair.getTwo() == RO) {
            throw new IllegalArgumentException("not writable register");
        }
        return pair.getOne();
    }

    private ByteRegister getReadableRegister(int address) {
        Pair<ByteRegister, Permission> pair = getRegister(address);
        if (pair.getTwo() == WO) {
            throw new IllegalArgumentException("not readable register");
        }
        return pair.getOne();
    }

    private Pair<ByteRegister, Permission> getRegister(int address) {
        if (address >= 0x2000 && address < 0x4000) {
            // PPU registers
            switch (address % 8) {
                case 0:
                    return pair(ppu.regPPUCTRL, WO);
                case 1:
                    return pair(ppu.regPPUMASK, WO);
                case 2:
                    return pair(ppu.regPPUSTATUS, RO);
                case 3:
                    return pair(ppu.regOAMADDR, WO);
                case 4:
                    return pair(ppu.regOAMDATA, RW);
                case 5:
                    return pair(ppu.regPPUSCROLL, WO);
                case 6:
                    return pair(ppu.regPPUADDR, WO);
                case 7:
                    return pair(ppu.regPPUDATA, RW);
            }
        } else {
            // APU and I/O registers
            switch (address) {
                case 0x4000:
                    return pair(apu.regSQ1_VOL, WO);
                case 0x4001:
                    return pair(apu.regSQ1_SWEEP, WO);
                case 0x4002:
                    return pair(apu.regSQ1_LO, WO);
                case 0x4003:
                    return pair(apu.regSQ1_HI, WO);
                case 0x4004:
                    return pair(apu.regSQ2_VOL, WO);
                case 0x4005:
                    return pair(apu.regSQ2_SWEEP, WO);
                case 0x4006:
                    return pair(apu.regSQ2_LO, WO);
                case 0x4007:
                    return pair(apu.regSQ2_HI, WO);
                case 0x4008:
                    return pair(apu.regTRI_LINEAR, WO);
                case 0x4009:
                    return pair(apu.regUNUSED1, RW);
                case 0x400A:
                    return pair(apu.regTRI_LO, WO);
                case 0x400B:
                    return pair(apu.regTRI_HI, WO);
                case 0x400C:
                    return pair(apu.regNOISE_VOL, WO);
                case 0x400D:
                    return pair(apu.regUNUSED2, RW);
                case 0x400E:
                    return pair(apu.regNOISE_LO, WO);
                case 0x400F:
                    return pair(apu.regNOISE_HI, WO);
                case 0x4010:
                    return pair(apu.regDMC_FREQ, WO);
                case 0x4011:
                    return pair(apu.regDMC_RAW, WO);
                case 0x4012:
                    return pair(apu.regDMC_START, WO);
                case 0x4013:
                    return pair(apu.regDMC_LEN, WO);
                case 0x4014:
                    return pair(cpu.regOAMDMA, WO);
                case 0x4015:
                    return pair(apu.regAPUSTATUS, WO);
                case 0x4016:
                    return pair(cpu.regJOY1, RW);
                case 0x4017:
                    return pair(cpu.regJOY2, RW);
            }
        }
        throw new IllegalArgumentException();
    }
}
