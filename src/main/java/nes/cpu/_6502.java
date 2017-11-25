package nes.cpu;

import common.*;
import nes.ppu.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static nes.cpu.AddressingMode.*;
import static nes.cpu.MemoryMapper.PROGRAM_OFFSET;

// http://hp.vector.co.jp/authors/VA042397/nes/index.html
public class _6502 {
    private static final int CODE_WIDTH = 8;
    private static final int RAM_SIZE= 2048;

    private final MemoryByte regA = new ByteRegister((byte)0);    // Accumulator
    private final MemoryByte regX = new ByteRegister((byte)0);    // X Index
    private final MemoryByte regY = new ByteRegister((byte)0);    // Y Index
    private final MemoryByte regS = new ByteRegister((byte)0);    // Stack Pointer
    private final FlagRegister regP = new FlagRegister();
    private final RegisterImpl regPC = new RegisterImpl(PROGRAM_OFFSET, 16);   // Program Counter

    final ByteArrayMemory programRom;

    private final MemoryMapper memoryMapper;

    public _6502(PPU ppu, ByteArrayMemory programRom) {
        memoryMapper = new MemoryMapper(this, ppu);
        this.programRom = programRom;
    }

    public void start() {
        while (true) {
            System.out.print(String.format("%04x ", regPC.get() * 1));
            byte code = getCode();
            Operation op = Operation.of(code);
            if (op == null) {
                System.out.print(BinaryUtil.toBinaryString(code, CODE_WIDTH) + "\n");
            }
            System.out.print(op.getOpcode().toString());
            System.out.print(" " + op.getAddressingMode().toString());
            switch (op.getAddressingMode().addressBytes) {
                case 0:
                    System.out.print("\n");
                    executeInstruction(op, null, null);
                    continue;
                case 1:
                    byte operand = getCode();
                    System.out.print(String.format(" %02x", operand));
                    System.out.print("\n");
                    executeInstruction(op, operand, null);
                    continue;
                case 2:
                    byte operand1 = getCode();
                    byte operand2 = getCode();
                    System.out.print(String.format(" %02x", operand2));
                    System.out.print(String.format(" %02x", operand1));
                    System.out.print("\n");
                    executeInstruction(op, operand1, operand2);
                    continue;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private byte getCode() {
        byte code = memoryMapper.get(regPC.get());
        regPC.increment();
        return code;
    }

    private void executeInstruction(Operation op, Byte operand1, Byte operand2) {
        switch (op.getOpcode()) {
            case SEI:
                regP.setInterruptDisable(true);
                break;
            case LDX:
                if (op.getAddressingMode() == IMMEDIATE) {
                    regX.set(operand1);
                    setZeroFlag(operand1);
                    setNegativeFlag(operand1);
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case LDY:
                if (op.getAddressingMode() == IMMEDIATE) {
                    regY.set(operand1);
                    setZeroFlag(operand1);
                    setNegativeFlag(operand1);
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case TXS:
                regS.set(regX.get());
                break;
            case LDA:
                Byte value = null;
                switch (op.getAddressingMode()) {
                    case IMMEDIATE:
                        value = operand1;
                        break;
                    case ABSOLUTE:
                        value = memoryMapper.get(getAddress(operand1, operand2));
                        break;
                    case ABSOLUTE_X:
                        value = memoryMapper.get(getAddress(operand1, operand2) + regX.get());
                        break;
                }
                checkNotNull(value);
                regA.set(value);
                setZeroFlag(value);
                setNegativeFlag(value);
                break;
            case STA:
                if (op.getAddressingMode() == ABSOLUTE) {
                    int address = getAddress(operand1, operand2);
                    memoryMapper.set(regA.get(), address);
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case INX:
                if (op.getAddressingMode() != IMPLICIT) {
                    throw new IllegalArgumentException();
                }
                incrementRegister(regX);
                break;
            case INY:
                if (op.getAddressingMode() != IMPLICIT) {
                    throw new IllegalArgumentException();
                }
                incrementRegister(regY);
                break;
            case DEX:
                if (op.getAddressingMode() != IMPLICIT) {
                    throw new IllegalArgumentException();
                }
                decrementRegister(regX);
                break;
            case DEY:
                if (op.getAddressingMode() != IMPLICIT) {
                    throw new IllegalArgumentException();
                }
                decrementRegister(regY);
                break;
            case BNE:
                checkArgument(op.getAddressingMode() == RELATIVE);
                if (!regP.isZero()) {
                    regPC.set(regPC.get() + operand1);
                }
                break;
            case BPL:
                checkArgument(op.getAddressingMode() == RELATIVE);
                if (!regP.isNegative()) {
                    regPC.set(regPC.get() + operand1);
                }
                break;

            case JMP:
                if (op.getAddressingMode() != ABSOLUTE) {
                    throw new IllegalArgumentException();
                }
                int newAddress = getAddress(operand1, operand2);
                if (newAddress == regPC.get() - 3) {
                    // FIXME stop if explicit infinite loop
                    throw new RuntimeException("Explicit infinite loop");
                }
                regPC.set(newAddress);
                break;

            case CLD:
                regP.setDecimal(false);
                break;
            case SED:
                regP.setDecimal(true);
                break;


            default:
                throw new IllegalArgumentException("Unsupported operation");
        }
    }

    private void incrementRegister(MemoryByte reg) {
        reg.increment();
        setZeroFlag(reg);
        setNegativeFlag(reg);
    }

    private void decrementRegister(MemoryByte reg) {
        reg.decrement();
        setZeroFlag(reg);
        setNegativeFlag(reg);
    }

    private void setZeroFlag(MemoryByte reg) {
        regP.setZero(reg.get() == 0);
    }

    private void setZeroFlag(byte v) {
        regP.setZero(v == 0);
    }

    private void setNegativeFlag(MemoryByte reg) {
        regP.setNegative(reg.getBit(7));
    }

    private void setNegativeFlag(byte v) {
        regP.setNegative(BinaryUtil.getBit(v, 7));
    }

    static public int getAddress(byte lower, byte upper) {
        return (Byte.toUnsignedInt(upper) << 8) + Byte.toUnsignedInt(lower);
    }
}
