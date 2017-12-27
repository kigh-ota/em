package nes.cpu;

import common.BinaryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @see http://obelisk.me.uk/6502/reference.html
// @see http://pgate1.at-ninja.jp/NES_on_FPGA/nes_cpu.htm
@RequiredArgsConstructor
@Slf4j
enum Instruction {
    ADC(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            byte a = cpu.getA();
            byte c = cpu.getCarryFlag() ? (byte)1 : (byte)0;
            int signedResult = a + value + c;
            boolean overflow = signedResult < -128 || signedResult > 127;
            int unsignedResult = Byte.toUnsignedInt(a) + Byte.toUnsignedInt(value) + c;
            boolean carry = BinaryUtil.getBit(unsignedResult, 8);
            byte newA = (byte)unsignedResult; // take last 8 bits
            cpu.setOverflowFlag(overflow);
            cpu.setCarryFlag(carry);
            cpu.setA(newA);
            cpu.setZeroFlag(newA);
            cpu.setNegativeFlag(newA);
        }
    }, // Add with Carry
    SBC(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            byte a = cpu.getA();
            byte c = cpu.getCarryFlag() ? (byte)0 : (byte)1;
            int signedResult = a - value - c;
            boolean overflow = signedResult < -128 || signedResult > 127;
            int unsignedResult = Byte.toUnsignedInt(a) - Byte.toUnsignedInt(value) - c;
            boolean carry = BinaryUtil.getBit(unsignedResult, 8);
            byte newA = (byte)unsignedResult;
            cpu.setOverflowFlag(overflow);
            cpu.setCarryFlag(!carry);
            cpu.setA(newA);
            cpu.setZeroFlag(newA);
            cpu.setNegativeFlag(newA);
        }
    }, // Subtract with Carry

    AND(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            byte result = (byte)(cpu.getA() & value);
            cpu.setA(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Logical AND
    ORA(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            byte result = (byte)(cpu.getA() | value);
            cpu.setA(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Logical Inclusive OR
    EOR(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            byte result = (byte)(cpu.getA() ^ value);
            cpu.setA(result);
            cpu.setZeroFlag(result);
            cpu.setNegativeFlag(result);
        }
    }, // Exclusive OR

    ASL(true) {
        @Override
        void execute(Integer address, Byte oldValue, CPU cpu) {
            byte newValue = (byte)(oldValue << 1);
            if (address == null) {
                cpu.setA(newValue);
            } else {
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.setCarryFlag(BinaryUtil.getBit(oldValue, 7));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);
        }
    }, // Arithmetic Shift Left
    LSR(true) {
        @Override
        void execute(Integer address, Byte oldValue, CPU cpu) {
            byte newValue = BinaryUtil.setBit(false, (byte)(oldValue >> 1), 7);
            if (address == null) {
                // accumulator
                cpu.setA(newValue);
            } else {
                // memory
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.setCarryFlag(BinaryUtil.getBit(oldValue, 0));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);
        }
    }, // Logical Shift Right
    ROL(true) {
        @Override
        void execute(Integer address, Byte oldValue, CPU cpu) {
            byte newValue = BinaryUtil.setBit(cpu.getCarryFlag(), (byte)(oldValue << 1), 0);
            if (address == null) {
                // accumulator
                cpu.setA(newValue);
            } else {
                // memory
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.setCarryFlag(BinaryUtil.getBit(oldValue, 7));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);

        }
    }, // Rotate Left
    ROR(true) {
        @Override
        void execute(Integer address, Byte oldValue, CPU cpu) {
            byte newValue = BinaryUtil.setBit(cpu.getCarryFlag(), (byte)(oldValue >> 1), 7);
            if (address == null) {
                // accumulator
                cpu.setA(newValue);
            } else {
                // memory
                cpu.memoryMapper.set(newValue, address);
            }
            cpu.setCarryFlag(BinaryUtil.getBit(oldValue, 0));
            cpu.setZeroFlag(newValue);
            cpu.setNegativeFlag(newValue);
        }
    }, // Rotate Right

    BCC(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (!cpu.getCarryFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Carry Clear
    BCS(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (cpu.getCarryFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Carry Set
    BEQ(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (cpu.getZeroFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Equal
    BMI(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (cpu.getNegativeFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Minus
    BNE(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (!cpu.getZeroFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Not Equal
    BPL(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (!cpu.getNegativeFlag()) {
                cpu.jump(address);
            }

        }
    }, // Branch if Positive
    BVC(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (!cpu.getOverflowFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Overflow Clear
    BVS(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            if (cpu.getOverflowFlag()) {
                cpu.jump(address);
            }
        }
    }, // Branch if Overflow Set

    BIT(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            byte masked = (byte)(Byte.toUnsignedInt(value) & Byte.toUnsignedInt(cpu.getA()));
            cpu.setZeroFlag(masked);
            cpu.setOverflowFlag(BinaryUtil.getBit(value, 6));
            cpu.setNegativeFlag(value);
        }
    }, // Bit Test

    JMP(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.jump(address);
        }
    }, // Jump
    JSR(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.push16(cpu.getPC() - 1);
            cpu.jump(address);
        }
    }, // Jump to Subroutine
    RTS(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            int returnTo = cpu.pull16() + 1;
            if (log.isDebugEnabled()) {
                log.debug("return to: {}", BinaryUtil.toHexString(returnTo));
            }
            cpu.jump(returnTo);
        }
    }, // Return from Subroutine

    BRK(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.handleBRK();
        }
    }, // Force Interrupt
    RTI(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.pullP();
            cpu.jump(cpu.pull16());
        }
    }, // Return from Interrupt

    CMP(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            compare(cpu.getA(), value, cpu);
        }
    }, // Compare
    CPX(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            compare(cpu.getX(), value, cpu);
        }
    }, // Compare X Register
    CPY(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            compare(cpu.getY(), value, cpu);
        }
    }, // Compare Y Register

    INC(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            incrementMemory(address, cpu);
        }
    }, // Increment Memory
    INX(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.incrementX();
            final byte x = cpu.getX();
            cpu.setZeroFlag(x);
            cpu.setNegativeFlag(x);
        }
    }, // Increment X Register
    INY(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.incrementY();
            final byte y = cpu.getY();
            cpu.setZeroFlag(y);
            cpu.setNegativeFlag(y);
        }
    }, // Increment Y Register
    DEC(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            decrementMemory(address, cpu);
        }
    }, // Decrement Memory
    DEX(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.decrementX();
            final byte x = cpu.getX();
            cpu.setZeroFlag(x);
            cpu.setNegativeFlag(x);
        }
    }, // Decrement X Register
    DEY(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.decrementY();
            final byte y = cpu.getY();
            cpu.setZeroFlag(y);
            cpu.setNegativeFlag(y);
        }
    }, // Decrement Y Register

    SEC(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setCarryFlag(true);
        }
    }, // Set Carry Flag
    CLC(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setCarryFlag(false);
        }
    }, // Clear Carry Flag
    SED(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setDecimalFlag(true);
        }
    }, // Set Decimal Flag
    CLD(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setDecimalFlag(false);
        }
    }, // Clear Decimal Mode
    SEI(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setInterruptDisableFlag(true);
        }
    }, // Set Interrupt Disable
    CLI(false), // Clear Interrupt Disable
    CLV(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setOverflowFlag(false);
        }
    }, // Clear Overflow Flag

    LDA(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setA(value);
            cpu.setZeroFlag(value);
            cpu.setNegativeFlag(value);
        }
    }, // Load Accumulator
    LDX(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setX(value);
            cpu.setZeroFlag(value);
            cpu.setNegativeFlag(value);
        }
    }, // Load X Register
    LDY(true) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setY(value);
            cpu.setZeroFlag(value);
            cpu.setNegativeFlag(value);
        }
    }, // Load Y Register
    STA(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte a = cpu.getA();
            if (log.isDebugEnabled()) {
                log.debug("{}={}", BinaryUtil.toHexString(address), BinaryUtil.toHexString(a));
            }
            store(a, address, cpu);
        }
    }, // Store Accumulator
    STX(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte x = cpu.getX();
            if (log.isDebugEnabled()) {
                log.debug("{}={}", BinaryUtil.toHexString(address), BinaryUtil.toHexString(x));
            }
            store(x, address, cpu);
        }
    }, // Store X Register
    STY(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte y = cpu.getY();
            if (log.isDebugEnabled()) {
                log.debug("{}={}", BinaryUtil.toHexString(address), BinaryUtil.toHexString(y));
            }
            store(y, address, cpu);
        }
    }, // Store Y Register

    TAX(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte a = cpu.getA();
            cpu.setX(a);
            cpu.setZeroFlag(a);
            cpu.setNegativeFlag(a);
        }
    }, // Transfer Accumulator to X
    TAY(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte a = cpu.getA();
            cpu.setY(a);
            cpu.setZeroFlag(a);
            cpu.setNegativeFlag(a);
        }
    }, // Transfer Accumulator to Y
    TSX(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte s = cpu.getS();
            cpu.setX(s);
            cpu.setZeroFlag(s);
            cpu.setNegativeFlag(s);
        }
    }, // Transfer Stack Pointer to X
    TXA(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte x = cpu.getX();
            cpu.setA(x);
            cpu.setZeroFlag(x);
            cpu.setNegativeFlag(x);
        }
    }, // Transfer X to Accumulator
    TYA(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            final byte y = cpu.getY();
            cpu.setA(y);
            cpu.setZeroFlag(y);
            cpu.setNegativeFlag(y);
        }
    }, // Transfer Y to Accumulator
    TXS(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.setS(cpu.getX());
        }
    }, // Transfer X to Stack Pointer

    PHA(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.pushA();
        }
    }, // Push Accumulator
    PLA(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.pullA();
        }
    }, // Pull Accumulator
    PHP(false), // Push Processor Status
    PLP(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
            cpu.pullP();
        }
    }, // Pull Processor Status

    NOP(false) {
        @Override
        void execute(Integer address, Byte value, CPU cpu) {
        }
    }; // No Operation

//    abstract void execute(CPU cpu);

    final boolean needsValue;

    void execute(Integer address, Byte value, CPU cpu) {
        throw new UnsupportedOperationException(this.toString());
    };

    void store(byte value, int address, CPU cpu) {
        cpu.memoryMapper.set(value, address);
    }

    void incrementMemory(int address, CPU cpu) {
        byte value = cpu.memoryMapper.increment(address);
        cpu.setZeroFlag(value);
        cpu.setNegativeFlag(value);
    }

    void decrementMemory(int address, CPU cpu) {
        byte value = cpu.memoryMapper.decrement(address);
        cpu.setZeroFlag(value);
        cpu.setNegativeFlag(value);
    }

    void compare(byte minuend, byte subtrahend, CPU cpu) {
        byte diff = (byte)(Byte.toUnsignedInt(minuend) - Byte.toUnsignedInt(subtrahend));
        cpu.setZeroFlag(diff);
        cpu.setNegativeFlag(diff);
        cpu.setCarryFlag(Byte.toUnsignedInt(minuend) >= Byte.toUnsignedInt(subtrahend));
    }
}
