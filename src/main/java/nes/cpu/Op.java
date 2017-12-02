package nes.cpu;

import common.BinaryUtil;
import common.ByteArrayMemory;
import common.MemoryByte;
import lombok.RequiredArgsConstructor;

// @see http://obelisk.me.uk/6502/reference.html
// @see http://pgate1.at-ninja.jp/NES_on_FPGA/nes_cpu.htm
@RequiredArgsConstructor
enum Op {
    ADC(true), // Add with Carry
    SBC(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            int resultInt = Byte.toUnsignedInt(cpu.regA.get()) - Byte.toUnsignedInt(value) - (cpu.regP.isCarry() ? 0 : 1);
            boolean overflow = resultInt < -128 || resultInt > 127;
            cpu.regP.setOverflow(overflow);
            cpu.regP.setCarry(!overflow);
            byte result = (byte)resultInt;
            cpu.regA.set(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Subtract with Carry

    AND(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte result = (byte)(cpu.regA.get() & value);
            cpu.regA.set(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Logical AND
    ORA(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte result = (byte)(cpu.regA.get() | value);
            cpu.regA.set(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Logical Inclusive OR
    EOR(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte result = (byte)(cpu.regA.get() ^ value);
            cpu.regA.set(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Exclusive OR

    ASL(true) {
        @Override
        void execute(Integer address, Byte oldValue, _6502 cpu) {
            byte newValue = (byte)(oldValue << 1);
            if (address == null) {
                cpu.regA.set(newValue);
            } else {
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.regP.setCarry(BinaryUtil.getBit(oldValue, 0));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);
        }
    }, // Arithmetic Shift Left
    LSR(true) {
        @Override
        void execute(Integer address, Byte oldValue, _6502 cpu) {
            byte newValue = BinaryUtil.setBit(false, (byte)(oldValue >> 1), 7);
            if (address == null) {
                // accumulator
                cpu.regA.set(newValue);
            } else {
                // memory
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.regP.setCarry(BinaryUtil.getBit(oldValue, 0));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);
        }
    }, // Logical Shift Right
    ROL(true) {
        @Override
        void execute(Integer address, Byte oldValue, _6502 cpu) {
            byte newValue = BinaryUtil.setBit(cpu.regP.isCarry(), (byte)(oldValue << 1), 0);
            if (address == null) {
                // accumulator
                cpu.regA.set(newValue);
            } else {
                // memory
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.regP.setCarry(BinaryUtil.getBit(oldValue, 7));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);

        }
    }, // Rotate Left
    ROR(true) {
        @Override
        void execute(Integer address, Byte oldValue, _6502 cpu) {
            byte newValue = BinaryUtil.setBit(cpu.regP.isCarry(), (byte)(oldValue >> 1), 7);
            if (address == null) {
                // accumulator
                cpu.regA.set(newValue);
            } else {
                // memory
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.regP.setCarry(BinaryUtil.getBit(oldValue, 0));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);
        }
    }, // Rotate Right

    BCC(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            if (!cpu.regP.isCarry()) {
                cpu.regPC.set(address);
            }
        }
    }, // Branch if Carry Clear
    BCS(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            if (cpu.regP.isCarry()) {
                cpu.regPC.set(address);
            }
        }
    }, // Branch if Carry Set
    BEQ(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            if (cpu.regP.isZero()) {
                cpu.regPC.set(address);
            }
        }
    }, // Branch if Equal
    BMI(false), // Branch if Minus
    BNE(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            if (!cpu.regP.isZero()) {
                cpu.regPC.set(address);
            }
        }
    }, // Branch if Not Equal
    BPL(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            if (!cpu.regP.isNegative()) {
                cpu.regPC.set(address);
            }

        }
    }, // Branch if Positive
    BVC(false), // Branch if Overflow Clear
    BVS(false), // Branch if Overflow Set

    BIT(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte masked = (byte)(Byte.toUnsignedInt(value) & Byte.toUnsignedInt(cpu.regA.get()));
            cpu.setZeroFlag(masked);
            cpu.regP.setOverflow(BinaryUtil.getBit(value, 6));
            cpu.setNegativeFlag(value);
        }
    }, // Bit Test

    JMP(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
//                if (address == regPC.get() - 3) {
//                    throw new RuntimeException("Explicit infinite loop"); // FIXME
//                }
            cpu.regPC.set(address);
        }
    }, // Jump
    JSR(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.push16(cpu.regPC.get() - 1);
            cpu.regPC.set(address);
        }
    }, // Jump to Subroutine
    RTS(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regPC.set(cpu.pull16() + 1);
        }
    }, // Return from Subroutine

    BRK(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.handleBRK();
        }
    }, // Force Interrupt
    RTI(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.pullP();
            cpu.regPC.set(cpu.pull16());
        }
    }, // Return from Interrupt

    CMP(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            compare(cpu.regA.get(), value, cpu);
        }
    }, // Compare
    CPX(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            compare(cpu.regX.get(), value, cpu);
        }
    }, // Compare X Register
    CPY(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            compare(cpu.regY.get(), value, cpu);
        }
    }, // Compare Y Register

    INC(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            incrementMemory(address, cpu);
        }
    }, // Increment Memory
    INX(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            incrementRegister(cpu.regX, cpu);
        }
    }, // Increment X Register
    INY(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            incrementRegister(cpu.regY, cpu);
        }
    }, // Increment Y Register
    DEC(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            decrementMemory(address, cpu);
        }
    }, // Decrement Memory
    DEX(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            decrementRegister(cpu.regX, cpu);
        }
    }, // Decrement X Register
    DEY(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            decrementRegister(cpu.regY, cpu);
        }
    }, // Decrement Y Register

    SEC(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regP.setCarry(true);
        }
    }, // Set Carry Flag
    CLC(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regP.setCarry(false);
        }
    }, // Clear Carry Flag
    SED(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regP.setDecimal(true);
        }
    }, // Set Decimal Flag
    CLD(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regP.setDecimal(false);
        }
    }, // Clear Decimal Mode
    SEI(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regP.setInterruptDisable(true);
        }
    }, // Set Interrupt Disable
    CLI(false), // Clear Interrupt Disable
    CLV(false), // Clear Overflow Flag

    LDA(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regA.set(value);
            cpu.setZeroFlag(value);
            cpu.setNegativeFlag(value);
        }
    }, // Load Accumulator
    LDX(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regX.set(value);
            cpu.setZeroFlag(value);
            cpu.setNegativeFlag(value);
        }
    }, // Load X Register
    LDY(true) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regY.set(value);
            cpu.setZeroFlag(value);
            cpu.setNegativeFlag(value);
        }
    }, // Load Y Register
    STA(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            store(cpu.regA.get(), address, cpu);
        }
    }, // Store Accumulator
    STX(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            store(cpu.regX.get(), address, cpu);
        }
    }, // Store X Register
    STY(false), // Store Y Register

    TAX(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte a = cpu.regA.get();
            cpu.regX.set(a);
            cpu.setZeroFlag(a);
            cpu.setNegativeFlag(a);
        }
    }, // Transfer Accumulator to X
    TAY(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte a = cpu.regA.get();
            cpu.regY.set(a);
            cpu.setZeroFlag(a);
            cpu.setNegativeFlag(a);
        }
    }, // Transfer Accumulator to Y
    TSX(false), // Transfer Stack Pointer to X
    TXA(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            byte x = cpu.regX.get();
            cpu.regA.set(x);
            cpu.setZeroFlag(x);
            cpu.setNegativeFlag(x);
        }
    }, // Transfer X to Accumulator
    TYA(false), // Transfer Y to Accumulator
    TXS(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.regS.set(cpu.regX.get());
        }
    }, // Transfer X to Stack Pointer

    PHA(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.pushA();
        }
    }, // Push Accumulator
    PLA(false) {
        @Override
        void execute(Integer address, Byte value, _6502 cpu) {
            cpu.pullA();
        }
    }, // Pull Accumulator
    PHP(false), // Push Processor Status
    PLP(false), // Pull Processor Status

    NOP(false); // No Operation

//    abstract void execute(_6502 cpu);

    final boolean needsValue;

    void execute(Integer address, Byte value, _6502 cpu) {
        throw new UnsupportedOperationException(this.toString());
    };

    void store(byte value, int address, _6502 cpu) {
        cpu.memoryMapper.set(value, address);
    }

    void incrementRegister(MemoryByte reg, _6502 cpu) {
        reg.increment();
        cpu.setZeroFlag(reg);
        cpu.setNegativeFlag(reg);
        System.out.println(String.format("  Result value=%02x", reg.get()));
    }

    void decrementRegister(MemoryByte reg, _6502 cpu) {
        reg.decrement();
        cpu.setZeroFlag(reg);
        cpu.setNegativeFlag(reg);
        System.out.println(String.format("  Result value=%02x", reg.get()));
    }

    void incrementMemory(int address, _6502 cpu) {
        byte value = cpu.memoryMapper.increment(address);
        cpu.setZeroFlag(value);
        cpu.setNegativeFlag(value);
    }

    void decrementMemory(int address, _6502 cpu) {
        byte value = cpu.memoryMapper.decrement(address);
        cpu.setZeroFlag(value);
        cpu.setNegativeFlag(value);
    }

    void compare(byte minuend, byte subtrahend, _6502 cpu) {
        byte diff = (byte)(Byte.toUnsignedInt(minuend) - Byte.toUnsignedInt(subtrahend));
        cpu.setZeroFlag(diff);
        cpu.setNegativeFlag(diff);
        cpu.regP.setCarry(Byte.toUnsignedInt(minuend) >= Byte.toUnsignedInt(subtrahend));
    }
}
