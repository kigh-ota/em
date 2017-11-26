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

    private static final int NMI_VECTOR_ADDRESS = 0xFFFA;
    private static final int RESET_VECTOR_ADDRESS = 0xFFFC;
    private static final int IRQ_BRK_VECTOR_ADDRESS = 0xFFFE;

    // https://wiki.nesdev.com/w/index.php/CPU_power_up_state
    final MemoryByte regA = new ByteRegister((byte)0);    // Accumulator
    final MemoryByte regX = new ByteRegister((byte)0);    // X Index
    final MemoryByte regY = new ByteRegister((byte)0);    // Y Index
    final MemoryByte regS = new ByteRegister((byte)0xFD);    // Stack Pointer
    final FlagRegister regP = new FlagRegister((byte)0x34);
    final RegisterImpl regPC = new RegisterImpl(PROGRAM_OFFSET, 16);   // Program Counter

    final MemoryByte regJOY1 = new ByteRegister((byte)0); // $4016
    final MemoryByte regJOY2 = new ByteRegister((byte)0); // $4017

    final ByteArrayMemory ram;
    final ByteArrayMemory programRom;

    final MemoryMapper memoryMapper;

    private boolean flagNMI;

    public _6502(PPU ppu, ByteArrayMemory programRom) {
        memoryMapper = new MemoryMapper(this, ppu);
        this.programRom = programRom;
        this.ram = new ByteArrayMemory(new byte[0x800]);
        flagNMI = false;
    }

    @Getter
    private long cycles = 0;

    public void start() {
        regPC.set(getAddress(memoryMapper.get(0xFFFC), memoryMapper.get(0xFFFD)));

        while (true) {
            cycles++;

            if (flagNMI) {
                handleNMI();
            }

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

    public void reserveNMI() {
        flagNMI = true;
    }

    private void handleNMI() {
        flagNMI = false;
        System.out.println("*** NMI ***");
        pushPC();
        regP.setBreakCommand(false);
        pushP();
        regP.setInterruptDisable(true);
        regPC.set(getAddress(memoryMapper.get(NMI_VECTOR_ADDRESS), memoryMapper.get(NMI_VECTOR_ADDRESS + 1)));
    }

    void handleBRK() {
        System.out.println("*** BRK ***");
        pushPC();
        regP.setBreakCommand(true);
        pushP();
        regP.setInterruptDisable(true);
        regPC.set(getAddress(memoryMapper.get(IRQ_BRK_VECTOR_ADDRESS), memoryMapper.get(IRQ_BRK_VECTOR_ADDRESS + 1)));
    }

    private void pushPC() {
        push16(regPC.get());
    }

    void push16(int value) {
        memoryMapper.set((byte)((value >> 4) & 0b1111), getStackAddress());
        regS.decrement();
        memoryMapper.set((byte)(value & 0b1111), getStackAddress());
        regS.decrement();
    }

    int pull16() {
        regS.increment();
        byte lower = memoryMapper.get(getStackAddress());
        regS.increment();
        byte upper = memoryMapper.get(getStackAddress());
        return getAddress(lower, upper);
    }

    private void pushP() {
        memoryMapper.set(regP.get(), getStackAddress());
        regS.decrement();
    }

    void pushA() {
        memoryMapper.set(regA.get(), getStackAddress());
        regS.decrement();
    }

    void pullA() {
        regS.increment();
        byte value = memoryMapper.get(getStackAddress());
        regA.set(value);
        setZeroFlag(value);
        setNegativeFlag(value);
    }

    private int getStackAddress() {
        return 0x0100 + Byte.toUnsignedInt(regS.get());
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
            case ACCUMULATOR:
                value = regA.get();
                break;
            case IMPLICIT:
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
        op.getOpcode().execute(address, value, this);
    }

    void setZeroFlag(MemoryByte reg) {
        regP.setZero(reg.get() == 0);
    }

    void setZeroFlag(byte v) {
        regP.setZero(v == 0);
    }

    void setNegativeFlag(MemoryByte reg) {
        regP.setNegative(reg.getBit(7));
    }

    void setNegativeFlag(byte v) {
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
