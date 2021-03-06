package nes.cpu;

import common.*;
import lombok.extern.slf4j.Slf4j;
import nes.Controller;
import nes.apu.APU;
import nes.cpu.register.FlagRegister;
import nes.cpu.register.JoystickRegister;
import nes.cpu.register.OAMDMARegister;
import nes.cpu.register.ProgramCounter;
import nes.ppu.PPU;

import static nes.cpu.MemoryMapper.PROGRAM_OFFSET;

// http://hp.vector.co.jp/authors/VA042397/nes/index.html
@Slf4j
public class CPU implements Runnable {
    private static final int CODE_WIDTH = 8;
    private static final int RAM_SIZE = 2048;

    private static final int NMI_VECTOR_ADDRESS = 0xFFFA;
    private static final int RESET_VECTOR_ADDRESS = 0xFFFC;
    private static final int IRQ_BRK_VECTOR_ADDRESS = 0xFFFE;

    private final OperationFactory operationFactory;

    // https://wiki.nesdev.com/w/index.php/CPU_power_up_state
    private final ByteRegister regA = new ByteRegisterImpl((byte)0);    // Accumulator
    private final ByteRegister regX = new ByteRegisterImpl((byte)0);    // X Index
    private final ByteRegister regY = new ByteRegisterImpl((byte)0);    // Y Index
    private final ByteRegister regS = new ByteRegisterImpl((byte)0xFD);    // Stack Pointer
    private final FlagRegister regP = new FlagRegister((byte)0x34);
    private final IntegerRegister regPC = new ProgramCounter(PROGRAM_OFFSET, 16);

    final OAMDMARegister regOAMDMA; // $4014
    final ByteRegister regJOY1; // $4016
    public final ByteRegister regJOY2 = new ByteRegisterImpl((byte)0); // $4017

    final ByteArrayMemory ram;
    final ByteArrayMemory programRom;

    private final MemoryMapper memoryMapper;

    private boolean flagNMI;
    // TODO handle IRQ

    public CPU(PPU ppu, APU apu, ByteArrayMemory programRom, Controller controller1) {
        operationFactory = new OperationFactory();
        memoryMapper = new MemoryMapper(this, ppu, apu);
        this.programRom = programRom;
        this.ram = new ByteArrayMemory(new byte[0x800]);
        this.regOAMDMA = new OAMDMARegister(this, ppu);
        flagNMI = false;
        regJOY1 = new JoystickRegister(controller1);
    }

    private long cycles;

    synchronized public long getCyclesSynchronized() {
        return cycles;
    }

    public void reset() {
        cycles = 0L;
        jump(getAddress(memoryMapper.get(RESET_VECTOR_ADDRESS), memoryMapper.get(RESET_VECTOR_ADDRESS + 1)));
    };

    public void runStep() {
        if (flagNMI) {
            handleNMI();
        }

        byte code = getCode();
        Operation op = operationFactory.get(code);
        if (op == null) {
            log.error(BinaryUtil.toBinaryString(code, CODE_WIDTH));
        }
        if (log.isDebugEnabled()) {
            log.debug("PC={} op={}({}:{}) [X={} Y={} A={} S={} P={}] cycle={}",
                    BinaryUtil.toHexString(regPC.get() - 1),
                    BinaryUtil.toHexString(code),
                    op.getInstruction().toString(),
                    op.getAddressingMode().toString(),
                    BinaryUtil.toHexString(getX()),
                    BinaryUtil.toHexString(getY()),
                    BinaryUtil.toHexString(getA()),
                    BinaryUtil.toHexString(getS()),
                    BinaryUtil.toBinaryString(regP.get(), 8),
                    cycles);
        }
        if (regPC.get() != 0x8058) {
            log.info(String.format("%04x %x %x %x %x %x %x", regPC.get() - 1, code, getA(), getX(), getY(), getS(), regP.get()));
        }

        cycles += op.getCycles();

        switch (op.getAddressingMode().addressBytes) {
            case 0:
                executeInstruction(op, null, null);
                return;
            case 1:
                byte operand = getCode();
                executeInstruction(op, operand, null);
                return;
            case 2:
                byte operand1 = getCode();
                byte operand2 = getCode();
                executeInstruction(op, operand1, operand2);
                return;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void run() {
        reset();

        while (true) {
            runStep();
        }
    }

    public void reserveNMI() {
        flagNMI = true;
    }

    // TODO cycles?
    private void handleNMI() {
        flagNMI = false;
        log.debug("*** NMI ***");
        pushPC();
        setBreakCommandFlag(false);
        pushP();
        setInterruptDisableFlag(true);
        jump(getAddress(memoryMapper.get(NMI_VECTOR_ADDRESS), memoryMapper.get(NMI_VECTOR_ADDRESS + 1)));
    }

    void handleBRK() {
        log.debug("*** BRK ***");
        pushPC();
        setBreakCommandFlag(true);
        pushP();
        setInterruptDisableFlag(true);
        jump(getAddress(memoryMapper.get(IRQ_BRK_VECTOR_ADDRESS), memoryMapper.get(IRQ_BRK_VECTOR_ADDRESS + 1)));
    }

    private void pushPC() {
        push16(regPC.get());
    }

    void push16(int value) {
        memoryMapper.set((byte)((value >> 8) & 0b11111111), getStackAddress());
        regS.decrement();
        memoryMapper.set((byte)(value & 0b11111111), getStackAddress());
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

    void pullP() {
        regS.increment();
        regP.set(memoryMapper.get(getStackAddress()));
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
                if (op.getInstruction().needsValue) {
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

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("  (");
            if (operand1 != null) {
                sb.append(BinaryUtil.toHexString(operand1));
            }
            if (operand2 != null) {
                sb.append(", ");
                sb.append(BinaryUtil.toHexString(operand2));
            }
            sb.append(")");
            if (address != null) {
                sb.append(String.format(" addr=%s", BinaryUtil.toHexString(address)));
            }
            if (value != null) {
                sb.append(String.format(" value=%s", BinaryUtil.toHexString(value)));
            }
            log.debug(sb.toString());
        }
        op.getInstruction().execute(address, value, this);
    }

    public byte read(int address) {
        return memoryMapper.get(address);
    }

    public void write(byte value, int address) {
        memoryMapper.set(value, address);
    }

    public byte increment(int address) {
        return memoryMapper.increment(address);
    }

    public byte decrement(int address) {
        return memoryMapper.decrement(address);
    }

    void setZeroFlag(ByteRegister reg) {
        setZeroFlag(reg.get() == 0);
    }

    void setZeroFlag(byte v) {
        setZeroFlag(v == 0);
    }

    void setNegativeFlag(ByteRegister reg) {
        setNegativeFlag(reg.getBit(7));
    }

    void setNegativeFlag(byte v) {
        setNegativeFlag(BinaryUtil.getBit(v, 7));
    }

    static public int getAddress(byte lower, byte upper) {
        return BinaryUtil.getAddress(lower, upper);
    }

    void jump(int address) {
        if (log.isDebugEnabled()) {
            log.debug("jump to {}", BinaryUtil.toHexString(address));
        }
        regPC.set(address);
    }

    int getPC() {
        return regPC.get();
    }

    void setNegativeFlag(boolean flag) {
        regP.setNegative(flag);
    }

    void setOverflowFlag(boolean flag) {
        regP.setOverflow(flag);
    }

    void setBreakCommandFlag(boolean flag) {
        regP.setBreakCommand(flag);
    }

    void setDecimalFlag(boolean flag) {
        regP.setDecimal(flag);
    }

    void setInterruptDisableFlag(boolean flag) {
        regP.setInterruptDisable(flag);
    }

    void setZeroFlag(boolean flag) {
        regP.setZero(flag);
    }

    void setCarryFlag(boolean flag) {
        regP.setCarry(flag);
    }

    boolean getNegativeFlag() {
        return regP.isNegative();
    }

    boolean getOverflowFlag() {
        return regP.isOverflow();
    }

    boolean getBreakCommandFlag() {
        return regP.isBreakCommand();
    }

    boolean getDecimalFlag() {
        return regP.isDecimal();
    }

    boolean getInterruptDisableFlag() {
        return regP.isInturruptDisable();
    }

    boolean getZeroFlag() {
        return regP.isZero();
    }

    boolean getCarryFlag() {
        return regP.isCarry();
    }

    byte getX() {
        return regX.get();
    }

    void setX(byte value) {
        regX.set(value);
    }

    void incrementX() {
        regX.increment();
    }

    void decrementX() {
        regX.decrement();
    }

    byte getY() {
        return regY.get();
    }

    void setY(byte value) {
        regY.set(value);
    }

    void incrementY() {
        regY.increment();
    }

    void decrementY() {
        regY.decrement();
    }

    byte getA() {
        return regA.get();
    }

    void setA(byte value) {
        regA.set(value);
    }

    byte getS() {
        return regS.get();
    }

    void setS(byte value) {
        regS.set(value);
    }
}
