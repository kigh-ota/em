package nes.cpu;

// 1 page = 256 byte

import com.google.common.base.Preconditions;
import common.BinaryUtil;

enum AddressingMode {
    IMPLICIT(0),
    ACCUMULATOR(0),
    IMMEDIATE(1) {   // 8 bit constant
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            validateOperands(operand1, operand2);
            return null;
        }
    },
    ZERO_PAGE(1) {   // 8 bit address ($0000-$00FF) (LOWER) => $00:LOWER
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            validateOperands(operand1, operand2);
            return BinaryUtil.getAddress(operand1, (byte) 0);
        }
    },
    ZERO_PAGE_X(1) { // (LOWER) => $00:LOWER+X
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            validateOperands(operand1, operand2);
            byte lower = BinaryUtil.add(operand1, cpu.getX()).getOne(); // no carry
            return BinaryUtil.getAddress(lower, (byte) 0);
        }
    },
    ZERO_PAGE_Y(1) {
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            validateOperands(operand1, operand2);
            byte lower = BinaryUtil.add(operand1, cpu.getY()).getOne(); // no carry
            return BinaryUtil.getAddress(lower, (byte) 0);
        }
    },
    RELATIVE(1) {
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            validateOperands(operand1, operand2);
            return cpu.getPC() + operand1;
        }
    },    // 8 bit offset (=> -126 to +129)
    ABSOLUTE(2) {
        @Override
        Integer getAddress(Byte lower, Byte upper, _6502 cpu) {
            validateOperands(lower, upper);
            return BinaryUtil.getAddress(lower, upper);
        }
    },    // 16 bit address ($0000-$FFFF) (LOWER,UPPER)
    ABSOLUTE_X(2) {
        @Override
        Integer getAddress(Byte lower, Byte upper, _6502 cpu) {
            validateOperands(lower, upper);
            return (BinaryUtil.getAddress(lower, upper) + Byte.toUnsignedInt(cpu.getX())) % 0x10000;
        }
    },  // (LOWER,UPPER) => UPPER:LOWER+X
    ABSOLUTE_Y(2) {
        @Override
        Integer getAddress(Byte lower, Byte upper, _6502 cpu) {
            validateOperands(lower, upper);
            return (BinaryUtil.getAddress(lower, upper) + Byte.toUnsignedInt(cpu.getY())) % 0x10000;
        }
    },
    INDIRECT(2) {
        @Override
        Integer getAddress(Byte pointerLower, Byte pointerUpper, _6502 cpu) {
            validateOperands(pointerLower, pointerUpper);
            byte lower = cpu.memoryMapper.get(BinaryUtil.getAddress(pointerLower, pointerUpper));
            byte upper = cpu.memoryMapper.get(BinaryUtil.getAddress(BinaryUtil.add(pointerLower, (byte)1).getOne(), pointerUpper));
            return BinaryUtil.getAddress(lower, upper);
        }
    }, // (LOWER,UPPER) => value at UPPER:LOWER
    INDEXED_INDIRECT_X(1) {
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            throw new UnsupportedOperationException();
        }
    }, // (LOWER) => value at $00:LOWER+X ??
    INDIRECT_INDEXED_Y(1) {
        @Override
        Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
            validateOperands(operand1, operand2);
            byte lower = cpu.memoryMapper.get(BinaryUtil.getAddress(operand1, (byte)0));
            byte upper = cpu.memoryMapper.get(BinaryUtil.getAddress(BinaryUtil.add(operand1, (byte)1).getOne(), (byte)0));
            return BinaryUtil.getAddress(lower, upper) + Byte.toUnsignedInt(cpu.getY());
        }
    }; // (LOWER) => value at((value at $00:LOWER)+Y)

    final int addressBytes;

    private AddressingMode(int addressBytes) {
        this.addressBytes = addressBytes;
    }

    Integer getAddress(Byte operand1, Byte operand2, _6502 cpu) {
        validateOperands(operand1, operand2);
        return null;
    }

    void validateOperands(Byte operand1, Byte operand2) {
        switch (addressBytes) {
            case 0:
                Preconditions.checkArgument(operand1 == null && operand2 == null);
                return;
            case 1:
                Preconditions.checkArgument(operand1 != null && operand2 == null);
                return;
            case 2:
                Preconditions.checkArgument(operand1 != null && operand2 != null);
                return;
            default:
                throw new IllegalStateException();
        }
    }
}
