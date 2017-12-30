package nes.cpu.register;

import common.ByteRegister;

public class FlagRegister extends ByteRegister {
    private static final int NEGATIVE_BIT = 7;
    private static final int OVERFLOW_BIT = 6;
    private static final int ONE_BIT = 5;
    private static final int BREAK_COMMAND_BIT = 4;
    private static final int DECIMAL_BIT = 3;
    private static final int INTERRUPT_DISABLE_BIT = 2;
    private static final int ZERO_BIT = 1;
    private static final int CARRY_BIT = 0;

    public FlagRegister(byte value) {
        super(value);
    }

    public void setNegative(boolean flag) {
        setBit(flag, NEGATIVE_BIT);
    }
    public void setOverflow(boolean flag) {
        setBit(flag, OVERFLOW_BIT);
    }
    public void setBreakCommand(boolean flag) {
        setBit(flag, BREAK_COMMAND_BIT);
    }
    public void setDecimal(boolean flag) {
        setBit(flag, DECIMAL_BIT);
    }
    public void setInterruptDisable(boolean flag) {
        setBit(flag, INTERRUPT_DISABLE_BIT);
    }
    public void setZero(boolean flag) {
        setBit(flag, ZERO_BIT);
    }
    public void setCarry(boolean flag) {
        setBit(flag, CARRY_BIT);
    }

    public boolean isNegative() {
        return getBit(NEGATIVE_BIT);
    }
    public boolean isOverflow() {
        return getBit(OVERFLOW_BIT);
    }
    public boolean isBreakCommand() {
        return getBit(BREAK_COMMAND_BIT);
    }
    public boolean isDecimal() {
        return getBit(DECIMAL_BIT);
    }
    public boolean isInturruptDisable() {
        return getBit(INTERRUPT_DISABLE_BIT);
    }
    public boolean isZero() {
        return getBit(ZERO_BIT);
    }
    public boolean isCarry() {
        return getBit(CARRY_BIT);
    }
}
