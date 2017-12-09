package nes.cpu;

import common.*;
import lombok.extern.slf4j.Slf4j;
import nes.Controller;
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
    final MemoryByte regA = new ByteRegister((byte)0);    // Accumulator
    final MemoryByte regX = new ByteRegister((byte)0);    // X Index
    final MemoryByte regY = new ByteRegister((byte)0);    // Y Index
    final MemoryByte regS = new ByteRegister((byte)0xFD);    // Stack Pointer
    final FlagRegister regP = new FlagRegister((byte)0x34);
    final RegisterImpl regPC = new RegisterImpl(PROGRAM_OFFSET, 16);   // Program Counter

    final MemoryByte regSQ1_VOL = new ByteRegister((byte)0); // $4000
    final MemoryByte regSQ1_SWEEP = new ByteRegister((byte)0); // $4001
    final MemoryByte regSQ1_LO = new ByteRegister((byte)0); // $4002
    final MemoryByte regSQ1_HI = new ByteRegister((byte)0); // $4003
    final MemoryByte regSQ2_VOL= new ByteRegister((byte)0); // $4004
    final MemoryByte regSQ2_SWEEP = new ByteRegister((byte)0); // $4005
    final MemoryByte regSQ2_LO = new ByteRegister((byte)0); // $4006
    final MemoryByte regSQ2_HI = new ByteRegister((byte)0); // $4007
    final MemoryByte regTRI_LINEAR = new ByteRegister((byte)0); // $4008
    final MemoryByte regUNUSED1 = new ByteRegister((byte)0); // $4009
    final MemoryByte regTRI_LO = new ByteRegister((byte)0); // $400A
    final MemoryByte regTRI_HI = new ByteRegister((byte)0); // $400B
    final MemoryByte regNOISE_VOL = new ByteRegister((byte)0); // $400C
    final MemoryByte regUNUSED2 = new ByteRegister((byte)0); // $400D
    final MemoryByte regNOISE_LO = new ByteRegister((byte)0); // $400E
    final MemoryByte regNOISE_HI = new ByteRegister((byte)0); // $400F
    final MemoryByte regDMC_FREQ = new ByteRegister((byte)0); // $4010
    final MemoryByte regDMC_RAW = new ByteRegister((byte)0); // $4011
    final MemoryByte regDMC_START = new ByteRegister((byte)0); // $4012
    final MemoryByte regDMC_LEN = new ByteRegister((byte)0); // $4013
    final OAMDMARegister regOAMDMA; // $4014
    final MemoryByte regSND_CHN = new ByteRegister((byte)0); // $4015
    final MemoryByte regJOY1; // $4016
    final MemoryByte regJOY2 = new ByteRegister((byte)0); // $4017

    final ByteArrayMemory ram;
    final ByteArrayMemory programRom;

    final MemoryMapper memoryMapper;

    private boolean flagNMI;

    public CPU(PPU ppu, ByteArrayMemory programRom, Controller controller1) {
        operationFactory = new OperationFactory();
        memoryMapper = new MemoryMapper(this, ppu);
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

    @Override
    public void run() {
        cycles = 0L;
        regPC.set(getAddress(memoryMapper.get(0xFFFC), memoryMapper.get(0xFFFD)));

        while (true) {
            if (flagNMI) {
                handleNMI();
            }

            byte code = getCode();
            Operation op = operationFactory.get(code);
            if (op == null) {
                log.error(BinaryUtil.toBinaryString(code, CODE_WIDTH));
            }
            log.debug("PC={} op={}({}) [A={} S={}] cycle={}", Integer.toHexString(regPC.get()), op.getOp().toString(), op.getAddressingMode().toString(),
                    Integer.toHexString(Byte.toUnsignedInt(regA.get())), Integer.toHexString(Byte.toUnsignedInt(regS.get())), cycles);

            cycles += op.getCycles();

            switch (op.getAddressingMode().addressBytes) {
                case 0:
                    executeInstruction(op, null, null);
                    continue;
                case 1:
                    byte operand = getCode();
                    log.debug(" operand={}", Integer.toHexString(Byte.toUnsignedInt(operand)));
                    executeInstruction(op, operand, null);
                    continue;
                case 2:
                    byte operand1 = getCode();
                    byte operand2 = getCode();
                    log.debug(" operand={} {}",
                            Integer.toHexString(Byte.toUnsignedInt(operand2)),
                            Integer.toHexString(Byte.toUnsignedInt(operand1)));
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

    // TODO cycles?
    private void handleNMI() {
        flagNMI = false;
        log.debug("*** NMI ***");
        pushPC();
        regP.setBreakCommand(false);
        pushP();
        regP.setInterruptDisable(true);
        regPC.set(getAddress(memoryMapper.get(NMI_VECTOR_ADDRESS), memoryMapper.get(NMI_VECTOR_ADDRESS + 1)));
    }

    void handleBRK() {
        log.debug("*** BRK ***");
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
                if (op.getOp().needsValue) {
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
            log.debug("  address=${}", Integer.toHexString(address));
        }
        if (value != null) {
            log.debug("  value=${}", Integer.toHexString(Byte.toUnsignedInt(value)));
        }
        op.getOp().execute(address, value, this);
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
