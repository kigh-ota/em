package nes.cpu;

import common.*;
import lombok.Getter;
import nes.ppu.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static nes.cpu.AddressingMode.*;
import static nes.cpu.MemoryMapper.PROGRAM_OFFSET;

// http://hp.vector.co.jp/authors/VA042397/nes/index.html
public class _6502 {
    private static final int CODE_WIDTH = 8;
    private static final int RAM_SIZE = 2048;

    // https://wiki.nesdev.com/w/index.php/CPU_power_up_state
    private final MemoryByte regA = new ByteRegister((byte)0);    // Accumulator
    private final MemoryByte regX = new ByteRegister((byte)0);    // X Index
    private final MemoryByte regY = new ByteRegister((byte)0);    // Y Index
    private final MemoryByte regS = new ByteRegister((byte)0xFD);    // Stack Pointer
    private final FlagRegister regP = new FlagRegister((byte)0x34);
    private final RegisterImpl regPC = new RegisterImpl(PROGRAM_OFFSET, 16);   // Program Counter

    final ByteArrayMemory ram;
    final ByteArrayMemory programRom;

    final MemoryMapper memoryMapper;

    public _6502(PPU ppu, ByteArrayMemory programRom) {
        memoryMapper = new MemoryMapper(this, ppu);
        this.programRom = programRom;
        this.ram = new ByteArrayMemory(new byte[0x800]);
    }

    @Getter
    private long cycles = 0;

    public void start() {
        regPC.set(getAddress(memoryMapper.get(0xFFFC), memoryMapper.get(0xFFFD)));

        while (true) {
            cycles++;

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
        Integer address = op.getAddressingMode().getAddress(operand1, operand2, this);
        Byte value = null;
        switch (op.getAddressingMode()) {
            case IMMEDIATE:
                value = operand1;
                break;
            case ZERO_PAGE:
            case ZERO_PAGE_X:
            case ZERO_PAGE_Y:
            case ABSOLUTE:
            case ABSOLUTE_X:
            case ABSOLUTE_Y:
            case INDIRECT:
            case INDEXED_INDIRECT_X:
            case INDIRECT_INDEXED_Y:
                if (op.getOpcode().needsValue) {
                    value = memoryMapper.get(address);
                }
                break;
            case IMPLICIT:
            case ACCUMULATOR:
            case RELATIVE:
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (address != null) {
            System.out.print(String.format("  address=$%04x\n", address));
        }
        if (value != null) {
            System.out.print(String.format("  value=$%02x\n", value));
        }

        switch (op.getOpcode()) {
            case SEI:
                regP.setInterruptDisable(true);
                break;
            case LDX:
                regX.set(value);
                setZeroFlag(value);
                setNegativeFlag(value);
                break;
            case LDY:
                regY.set(value);
                setZeroFlag(value);
                setNegativeFlag(value);
                break;
            case TXS:
                regS.set(regX.get());
                break;
            case LDA:
                regA.set(value);
                setZeroFlag(value);
                setNegativeFlag(value);
                break;
            case STA:
                memoryMapper.set(regA.get(), address);
                break;
            case STX:
                memoryMapper.set(regX.get(), address);
                break;
            case INX:
                incrementRegister(regX);
                break;
            case INY:
                incrementRegister(regY);
                break;
            case DEX:
                decrementRegister(regX);
                break;
            case DEY:
                decrementRegister(regY);
                break;
            case BNE:
                if (!regP.isZero()) {
                    regPC.set(regPC.get() + operand1);
                }
                break;
            case BPL:
                if (!regP.isNegative()) {
                    regPC.set(regPC.get() + operand1);
                }
                break;
            case BCC:
                if (!regP.isCarry()) {
                    regPC.set(regPC.get() + operand1);
                }
                break;

            case JMP:
                if (address == regPC.get() - 3) {
                    throw new RuntimeException("Explicit infinite loop"); // FIXME
                }
                regPC.set(address);
                break;

            case CLD:
                regP.setDecimal(false);
                break;
            case SED:
                regP.setDecimal(true);
                break;

            case BRK:
                int pc = regPC.get();
                memoryMapper.set((byte)((pc >> 4) & 0b1111), 0x0100 + regS.get());
                regS.decrement();
                memoryMapper.set((byte)(pc & 0b1111), 0x0100 + regS.get());
                regS.decrement();
                regP.setBreakCommand(true);
                memoryMapper.set(regP.get(), regS.get());
                regS.decrement();
                regP.setInterruptDisable(true);
                regPC.set(getAddress(memoryMapper.get(0xFFFE), memoryMapper.get(0xFFFF)));
                break;

            case BIT:
                byte masked = (byte)(Byte.toUnsignedInt(value) & Byte.toUnsignedInt(regA.get()));
                setZeroFlag(masked);
                regP.setOverflow(BinaryUtil.getBit(value, 6));
                setNegativeFlag(value);
                break;

            case TAX:
                value = regA.get();
                regX.set(value);
                setZeroFlag(value);
                setNegativeFlag(value);
                break;

            case CPX:
                if (op.getAddressingMode() == IMMEDIATE) {
                    value = operand1;
                } else {
                    throw new IllegalArgumentException();
                }
                byte diff = (byte)(Byte.toUnsignedInt(regX.get()) - Byte.toUnsignedInt(value));
                setZeroFlag(diff);
                setNegativeFlag(diff);
                regP.setCarry(Byte.toUnsignedInt(regX.get()) >= Byte.toUnsignedInt(value));
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
        return BinaryUtil.getAddress(lower, upper);
    }

    byte getX() {
        return regX.get();
    }
    byte getY() {
        return regX.get();
    }
    int getPC() {
        return regPC.get();
    }
}
