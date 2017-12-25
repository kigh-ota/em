package nes.cpu;

import common.ByteRegisterImpl;

class FlagRegister extends ByteRegisterImpl {
    private static final int NEGATIVE_BIT = 7;
    private static final int OVERFLOW_BIT = 6;
    private static final int ONE_BIT = 5;
    private static final int BREAK_COMMAND_BIT = 4;
    private static final int DECIMAL_BIT = 3;
    private static final int INTERRUPT_DISABLE_BIT = 2;
    private static final int ZERO_BIT = 1;
    private static final int CARRY_BIT = 0;

    FlagRegister(byte value) {
        super(value);
    }

    void setNegative(boolean flag) {
        setBit(flag, NEGATIVE_BIT);
    }
    void setOverflow(boolean flag) {
        setBit(flag, OVERFLOW_BIT);
    }
    void setBreakCommand(boolean flag) {
        setBit(flag, BREAK_COMMAND_BIT);
    }
    void setDecimal(boolean flag) {
        setBit(flag, DECIMAL_BIT);
    }
    void setInterruptDisable(boolean flag) {
        setBit(flag, INTERRUPT_DISABLE_BIT);
    }
    void setZero(boolean flag) {
        setBit(flag, ZERO_BIT);
    }
    void setCarry(boolean flag) {
        setBit(flag, CARRY_BIT);
    }

    boolean isNegative() {
        return getBit(NEGATIVE_BIT);
    }
    boolean isOverflow() {
        return getBit(OVERFLOW_BIT);
    }
    boolean isBreakCommand() {
        return getBit(BREAK_COMMAND_BIT);
    }
    boolean isDecimal() {
        return getBit(DECIMAL_BIT);
    }
    boolean isInturruptDisable() {
        return getBit(INTERRUPT_DISABLE_BIT);
    }
    boolean isZero() {
        return getBit(ZERO_BIT);
    }
    boolean isCarry() {
        return getBit(CARRY_BIT);
    }
}
